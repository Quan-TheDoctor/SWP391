import cv2
import os
import sys
import time

def setup_paths():
    """
    Sets up necessary file paths and downloads required files if needed.

    Returns:
        dict: Dictionary containing all the important paths
    """
    script_dir = os.path.dirname(os.path.abspath(__file__))

    paths = {
        "script_dir": script_dir,
        "dataset_path": os.path.join(script_dir, 'datasets'),
        "cascade_path": os.path.join(script_dir, 'haarcascade_frontalface_default.xml')
    }

    if not os.path.exists(paths["cascade_path"]):
        print(f"Error: Cascade file not found. Downloading it now...")
        import urllib.request
        url = "https://raw.githubusercontent.com/opencv/opencv/master/data/haarcascades/haarcascade_frontalface_default.xml"
        urllib.request.urlretrieve(url, paths["cascade_path"])
        print(f"Downloaded face detection file successfully!")

    os.makedirs(paths["dataset_path"], exist_ok=True)

    return paths

def initialize_camera():
    """
    Sets up and initializes the camera.

    Returns:
        VideoCapture: Initialized camera object
    """
    cam = cv2.VideoCapture(0)
    cam.set(3, 640)  # Width
    cam.set(4, 480)  # Height
    return cam

def detect_faces(image, face_detector):
    """
    Detects faces in an image with improved parameters.

    Args:
        image: Image to detect faces in
        face_detector: OpenCV face detector

    Returns:
        tuple: Grayscale image and detected faces
    """
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    gray = cv2.equalizeHist(gray)

    faces = face_detector.detectMultiScale(
        gray,
        scaleFactor=1.1,
        minNeighbors=4,
        minSize=(30, 30)
    )

    return gray, faces

def save_face_image(gray, x, y, w, h, user_path, user_id, count):
    """
    Saves a detected face image to the user's directory.

    Args:
        gray: Grayscale image
        x, y, w, h: Face coordinates and dimensions
        user_path: Path to save the image
        user_id: ID of the user
        count: Current image count

    Returns:
        str: Path where the image was saved
    """
    face_roi = gray[y:y+h, x:x+w]

    file_path = os.path.join(user_path, f"User.{user_id}.{count}.jpg")
    cv2.imwrite(file_path, face_roi)

    return file_path

def create_user_info(user_path, user_id, user_name, count):
    """
    Creates an info file with user details.

    Args:
        user_path: Path to the user's directory
        user_id: ID of the user
        user_name: Name of the user
        count: Number of photos taken
    """
    with open(os.path.join(user_path, "info.txt"), "w") as f:
        f.write(f"Name: {user_name if user_name else 'User '+str(user_id)}\n")
        f.write(f"ID: {user_id}\n")
        f.write(f"Date: {time.strftime('%Y-%m-%d %H:%M:%S')}\n")
        f.write(f"Photos: {count}\n")

def take_photos(user_id, user_name=None):
    """
    Captures photos for a user with improved quality for better recognition.

    This function:
    1. Opens your camera
    2. Looks for your face
    3. Takes multiple photos of your face from different angles
    4. Saves these photos to use for recognizing you later

    Args:
        user_id (str): The ID of the user
        user_name (str, optional): The name of the user
    """
    paths = setup_paths()

    face_detector = cv2.CascadeClassifier(paths["cascade_path"])
    if face_detector.empty():
        print(f"Error: Failed to load face detector")
        return False

    user_path = os.path.join(paths["dataset_path"], str(user_id))
    os.makedirs(user_path, exist_ok=True)

    cam = initialize_camera()

    if user_name:
        print(f"Starting photo capture for User ID: {user_id} ({user_name})")
    else:
        print(f"Starting photo capture for User ID: {user_id}")

    max_photos = 50
    min_face_size = 100
    count = 0

    print(f"Will capture {max_photos} photos automatically")
    print("Move your face slowly in different angles for better recognition")

    print("Preparing camera... Starting in 3 seconds")
    for i in range(3, 0, -1):
        print(f"{i}...")
        time.sleep(1)

    try:
        while True:
            ret, img = cam.read()
            if not ret:
                print("Failed to grab frame")
                break

            height, width, _ = img.shape

            gray, faces = detect_faces(img, face_detector)

            for (x, y, w, h) in faces:
                face_quality = "Good" if w >= min_face_size else "Too small"
                quality_color = (0, 255, 0) if face_quality == "Good" else (0, 0, 255)

                cv2.rectangle(img, (x, y), (x+w, y+h), quality_color, 2)
                cv2.putText(img, face_quality, (x, y-10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, quality_color, 2)

                if count < max_photos and face_quality == "Good":
                    save_face_image(gray, x, y, w, h, user_path, user_id, count)
                    print(f"Saved image {count+1}/{max_photos}")
                    count += 1
                    time.sleep(0.2)

            cv2.putText(img, "Move your face slowly in different angles", (10, 30),
                        cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 0), 2)

            count_text = f"Photos: {count}/{max_photos}"
            text_size = cv2.getTextSize(count_text, cv2.FONT_HERSHEY_SIMPLEX, 1, 2)[0]
            text_x = (width - text_size[0]) // 2
            text_y = height - 20

            overlay = img.copy()
            cv2.rectangle(overlay, (text_x - 10, text_y - text_size[1] - 10),
                          (text_x + text_size[0] + 10, text_y + 10), (0, 0, 0), -1)
            cv2.addWeighted(overlay, 0.5, img, 0.5, 0, img)

            cv2.putText(img, count_text, (text_x, text_y),
                        cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 255, 255), 2)

            cv2.imshow('Camera', img)

            if count >= max_photos:
                print("Photo capture complete!")
                break

            k = cv2.waitKey(100) & 0xff
            if k == 27:
                print("Capture canceled by user")
                break

    except Exception as e:
        print(f"Error during photo capture: {str(e)}")

    finally:
        cam.release()
        cv2.destroyAllWindows()

        if os.path.exists(user_path):
            create_user_info(user_path, user_id, user_name, count)

        print(f"Captured {count} photos for user {user_id}")
        return count > 0

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python take_photos.py <user_id> [user_name]")
        sys.exit(1)

    user_id = sys.argv[1]
    user_name = sys.argv[2] if len(sys.argv) > 2 else None

    take_photos(user_id, user_name)
