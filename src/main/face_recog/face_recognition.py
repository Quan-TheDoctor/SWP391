import cv2
import numpy as np
import os
import sys
import time
import json
import requests
from datetime import datetime
from collections import Counter

def setup_paths():
    """
    Gets all the important file paths needed for the program.

    Returns:
        dict: Dictionary containing all necessary paths
    """
    script_dir = os.path.dirname(os.path.abspath(__file__))

    paths = {
        "script_dir": script_dir,
        "trainer_path": os.path.join(script_dir, 'trainer'),
        "dataset_path": os.path.join(script_dir, 'datasets'),
        "model_path": os.path.join(script_dir, 'trainer', 'trainer.yml'),
        "cascade_path": os.path.join(script_dir, 'haarcascade_frontalface_default.xml'),
        "attendance_path": os.path.join(script_dir, 'attendance')
    }

    return paths

def check_files(paths):
    """
    Checks if necessary files exist and downloads them if needed.

    Args:
        paths (dict): Dictionary of file paths

    Returns:
        bool: True if all files are ready, False if there's a problem
    """
    if not os.path.exists(paths["model_path"]):
        print("Error: Model file not found. Please train the model first.")
        return False

    if not os.path.exists(paths["cascade_path"]):
        print(f"Error: Face detection file not found. Downloading it now...")
        import urllib.request
        url = "https://raw.githubusercontent.com/opencv/opencv/master/data/haarcascades/haarcascade_frontalface_default.xml"
        urllib.request.urlretrieve(url, paths["cascade_path"])
        print(f"Downloaded face detection file successfully!")

    return True

def load_user_names(dataset_path):
    """
    Loads the names of all users from their info files.

    Args:
        dataset_path (str): Path to the datasets folder

    Returns:
        dict: Dictionary mapping user IDs to their names
    """
    user_names = {}

    for user_id in os.listdir(dataset_path):
        user_path = os.path.join(dataset_path, user_id)
        if os.path.isdir(user_path):
            info_path = os.path.join(user_path, "info.txt")
            if os.path.exists(info_path):
                with open(info_path, 'r') as info_file:
                    for line in info_file:
                        if line.startswith("Name:"):
                            user_names[int(user_id)] = line.replace("Name:", "").strip()
                            break

    return user_names

def setup_attendance(paths):
    """
    Sets up the attendance tracking system.

    Args:
        paths (dict): Dictionary of file paths

    Returns:
        tuple: Attendance file path and existing attendance data
    """
    os.makedirs(paths["attendance_path"], exist_ok=True)

    current_date = datetime.now().strftime("%Y-%m-%d")
    attendance_file = os.path.join(paths["attendance_path"], f"attendance_{current_date}.json")

    attendance_data = {}
    if os.path.exists(attendance_file):
        with open(attendance_file, 'r') as f:
            try:
                attendance_data = json.load(f)
            except json.JSONDecodeError:
                attendance_data = {}

    return attendance_file, attendance_data

def detect_faces(gray_image, cascade_path):
    """
    Detects faces in the grayscale image.

    Args:
        gray_image (numpy.ndarray): Grayscale image
        cascade_path (str): Path to the face detection cascade file

    Returns:
        list: List of face locations (x, y, width, height)
    """
    face_detector = cv2.CascadeClassifier(cascade_path)

    faces = face_detector.detectMultiScale(
        gray_image,
        scaleFactor=1.1,
        minNeighbors=4,
        minSize=(30, 30),
        flags=cv2.CASCADE_SCALE_IMAGE
    )

    return faces

def preprocess_face(gray_image, x, y, w, h):
    """
    Prepares a face image for better recognition.

    Args:
        gray_image (numpy.ndarray): Grayscale image
        x, y, w, h: Face coordinates and dimensions

    Returns:
        numpy.ndarray: Processed face image
    """
    face_roi = gray_image[y:y+h, x:x+w]
    face_roi = cv2.GaussianBlur(face_roi, (5, 5), 0)
    return face_roi

def recognize_face(recognizer, face_roi, recent_predictions, face_key, prediction_window):
    """
    Recognizes a face using the trained model.

    Args:
        recognizer: Face recognizer model
        face_roi (numpy.ndarray): Face image
        recent_predictions (dict): Dictionary of recent predictions
        face_key (str): Key for this face
        prediction_window (int): Number of frames to average

    Returns:
        tuple: User ID, confidence, and confidence text
    """
    try:
        id, confidence = recognizer.predict(face_roi)

        if face_key not in recent_predictions:
            recent_predictions[face_key] = []

        if np.isfinite(confidence):
            recent_predictions[face_key].append((id, confidence))
            if len(recent_predictions[face_key]) > prediction_window:
                recent_predictions[face_key].pop(0)

        if recent_predictions[face_key]:
            ids = [pred[0] for pred in recent_predictions[face_key]]
            confidences = [pred[1] for pred in recent_predictions[face_key]]

            most_common_id = Counter(ids).most_common(1)[0][0]
            avg_confidence = sum(confidences) / len(confidences)

            id = most_common_id
            confidence = avg_confidence

            if not np.isfinite(confidence):
                confidence = 100

            confidence_value = max(0, min(100, 100 - confidence))
            confidence_text = f"{int(confidence_value)}%"

            return id, confidence, confidence_text
        else:
            return None, None, "0%"

    except Exception as e:
        print(f"Error during recognition: {str(e)}")
        return None, None, "Error"

def update_attendance(id, name, attendance_data, attendance_file, recognized_users, confidence_text, recognition_type):
    """
    Updates the attendance record for a recognized user and sends data to Spring Boot API.

    Args:
        id: User ID
        name (str): User name
        attendance_data (dict): Current attendance data
        attendance_file (str): Path to attendance file
        recognized_users (set): Set of already recognized users
        confidence_text (str): Confidence level as text
        recognition_type (str): type of attendance

    Returns:
        tuple: Updated set of recognized users and boolean indicating if API call was successful
    """
    api_success = False
    recognized_name = None

    if id not in recognized_users:
        timestamp = datetime.now().strftime("%H:%M:%S")
        date_str = datetime.now().strftime("%Y-%m-%d")

        if str(id) not in attendance_data:
            attendance_data[str(id)] = {
                "name": name,
                "check_in": timestamp,
                "check_out": None
            }
        else:
            attendance_data[str(id)]["check_out"] = timestamp

        with open(attendance_file, 'w') as f:
            json.dump(attendance_data, f, indent=4)

        recognized_users.add(id)
        recognized_name = name
        print(f"Recognized: {name} (ID: {id}) - {confidence_text}")

        try:
            recognition_data = {
                "id": str(id),
                "name": name,
                "timestamp": f"{date_str} {timestamp}",
                "confidence": confidence_text,
                "type": recognition_type
            }

            print(f"Sending recognition data to API: {recognition_data}")

            response = requests.post(
                "http://localhost:8081/api/face-recognition/recognition-success",
                json=recognition_data,
                timeout=1
            )

            print(f"Response status: {response.status_code}")
            print(f"Response content: {response.text}")

            if response.status_code == 200:
                print("Recognition data sent to Spring Boot API successfully")
                api_success = True
            else:
                print(f"Failed to send recognition data: {response.status_code} - {response.text}")

        except Exception as e:
            print(f"Error sending recognition data to Spring Boot API: {str(e)}")
            import traceback
            traceback.print_exc()

    return recognized_users, api_success, recognized_name

def recognize_faces():
    """
    Main function that runs the face recognition system.
    This program:
    1. Opens your camera
    2. Looks for faces
    3. Tries to recognize who each face belongs to
    4. Records when people arrive and leave
    5. Shows a success message when recognition is successful
    6. Automatically stops after 3 seconds of successful recognition

    Returns:
        bool: True if recognition completed successfully
    """

    recognition_type = "default"
    if len(sys.argv) > 1:
        recognition_type = sys.argv[1]
    paths = setup_paths()

    if not check_files(paths):
        return False

    recognizer = cv2.face.LBPHFaceRecognizer_create()
    recognizer.read(paths["model_path"])

    cam = cv2.VideoCapture(0)
    cam.set(3, 640)
    cam.set(4, 480)

    min_confidence = 80

    font = cv2.FONT_HERSHEY_SIMPLEX

    user_names = load_user_names(paths["dataset_path"])

    attendance_file, attendance_data = setup_attendance(paths)

    recognized_users = set()
    recent_predictions = {}
    prediction_window = 10

    api_success = False
    recognized_name = None
    success_time = None

    print("Starting face recognition...")
    print("Press 'q' to quit or wait for successful recognition")

    ret, img = cam.read()
    if ret:
        cv2.imshow('Face Recognition', img)
        cv2.waitKey(1)
        time.sleep(0.5)

    while True:
        ret, img = cam.read()
        if not ret:
            print("Failed to grab frame")
            break

        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        gray = cv2.equalizeHist(gray)

        display_img = img.copy()

        if api_success:
            elapsed = time.time() - success_time
            remaining = max(0, 5 - elapsed)

            overlay = display_img.copy()
            cv2.rectangle(overlay, (0, 0), (display_img.shape[1], display_img.shape[0]), (0, 0, 0), -1)

            success_text = f"Attendance recorded: {recognized_name}"
            closing_text = f"Close after {int(remaining)}s"

            text_size = cv2.getTextSize(success_text, font, 1, 2)[0]
            text_x = (display_img.shape[1] - text_size[0]) // 2
            text_y = (display_img.shape[0] + text_size[1]) // 2 - 30

            close_size = cv2.getTextSize(closing_text, font, 0.7, 1)[0]
            close_x = (display_img.shape[1] - close_size[0]) // 2
            close_y = text_y + 40

            cv2.putText(overlay, success_text, (text_x, text_y), font, 1, (0, 255, 0), 2)
            cv2.putText(overlay, closing_text, (close_x, close_y), font, 0.7, (255, 255, 255), 1)

            alpha = 0.7
            cv2.addWeighted(overlay, alpha, display_img, 1 - alpha, 0, display_img)

            cv2.imshow('Face Recognition', display_img)

            if elapsed >= 5:
                print("Closing after successful recognition")
                break
        else:
            faces = detect_faces(gray, paths["cascade_path"])

            for (x, y, w, h) in faces:
                cv2.rectangle(display_img, (x, y), (x+w, y+h), (0, 255, 0), 2)

                face_roi = preprocess_face(gray, x, y, w, h)

                face_key = f"{x}_{y}_{w}_{h}"
                id, confidence, confidence_text = recognize_face(recognizer, face_roi, recent_predictions, face_key, prediction_window)

                if id is not None and confidence is not None:
                    if confidence < min_confidence:
                        if id in user_names:
                            name = user_names[id]
                        else:
                            name = f"User {id}"

                        recognized_users, current_api_success, current_name = update_attendance(id, name, attendance_data, attendance_file, recognized_users, confidence_text, recognition_type)

                        if current_api_success:
                            api_success = True
                            recognized_name = current_name
                            success_time = time.time()
                    else:
                        name = "Unknown"
                else:
                    name = "Unknown"

                cv2.putText(display_img, name, (x+5, y-5), font, 1, (255, 255, 255), 2)
                cv2.putText(display_img, confidence_text, (x+5, y+h-5), font, 1, (255, 255, 0), 1)

            cv2.imshow('Face Recognition', display_img)

        k = cv2.waitKey(10) & 0xff
        if k == ord('q'):
            print("Recognition stopped by user")
            break

    cam.release()
    cv2.destroyAllWindows()

    return True

if __name__ == "__main__":
    recognize_faces()
