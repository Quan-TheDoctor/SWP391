import cv2
import numpy as np
from PIL import Image
import os
import sys

def setup_paths():
    """
    Sets up necessary file paths and directories.

    Returns:
        dict: Dictionary containing all the important paths
    """
    script_dir = os.path.dirname(os.path.abspath(__file__))

    paths = {
        "script_dir": script_dir,
        "dataset_path": os.path.join(script_dir, 'datasets'),
        "trainer_path": os.path.join(script_dir, 'trainer'),
        "cascade_path": os.path.join(script_dir, 'haarcascade_frontalface_default.xml')
    }

    os.makedirs(paths["trainer_path"], exist_ok=True)

    if not os.path.exists(paths["dataset_path"]):
        print(f"Error: Dataset directory not found at {paths['dataset_path']}")
        return None

    if not os.path.exists(paths["cascade_path"]):
        print(f"Error: Cascade file not found. Downloading it now...")
        import urllib.request
        url = "https://raw.githubusercontent.com/opencv/opencv/master/data/haarcascades/haarcascade_frontalface_default.xml"
        urllib.request.urlretrieve(url, paths["cascade_path"])
        print(f"Downloaded face detection file successfully!")

    return paths

def preprocess_image(img_numpy):
    """
    Applies preprocessing to improve image quality for training.

    Args:
        img_numpy: NumPy array of the image

    Returns:
        numpy.ndarray: Processed image
    """
    return cv2.equalizeHist(img_numpy)

def preprocess_face(face_roi):
    """
    Applies preprocessing to improve face quality for training.

    Args:
        face_roi: Face region of interest

    Returns:
        numpy.ndarray: Processed face
    """
    return cv2.GaussianBlur(face_roi, (5, 5), 0)

def get_images_and_labels(dataset_path, detector):
    """
    Gets face images and their corresponding labels from the dataset.

    Args:
        dataset_path: Path to the dataset directory
        detector: Face detector

    Returns:
        tuple: Lists of face samples and their IDs
    """
    image_paths = []

    for user_id in os.listdir(dataset_path):
        user_path = os.path.join(dataset_path, user_id)
        if os.path.isdir(user_path):
            for img_file in os.listdir(user_path):
                if img_file.endswith('.jpg') or img_file.endswith('.png'):
                    image_paths.append(os.path.join(user_path, img_file))

    face_samples = []
    ids = []

    for image_path in image_paths:
        try:
            pil_img = Image.open(image_path).convert('L')
            img_numpy = np.array(pil_img, 'uint8')

            img_numpy = preprocess_image(img_numpy)

            user_id = int(os.path.split(image_path)[0].split(os.path.sep)[-1])

            faces = detector.detectMultiScale(
                img_numpy,
                scaleFactor=1.1,
                minNeighbors=4,
                minSize=(30, 30)
            )

            for (x, y, w, h) in faces:
                face_roi = img_numpy[y:y+h, x:x+w]
                face_roi = preprocess_face(face_roi)

                face_samples.append(face_roi)
                ids.append(user_id)

        except Exception as e:
            print(f"Error processing image {image_path}: {str(e)}")

    return face_samples, ids

def save_user_details(trainer_path, dataset_path, users, face_count):
    """
    Saves details about the trained users to a file.

    Args:
        trainer_path: Path to save the details
        dataset_path: Path to the dataset
        users: Set of user IDs
        face_count: Number of face samples
    """
    with open(os.path.join(trainer_path, 'users.txt'), 'w') as f:
        f.write(f"Total users: {len(users)}\n")
        f.write(f"Total samples: {face_count}\n")
        f.write("User IDs: " + ", ".join(map(str, sorted(users))) + "\n")

        f.write("\nUser Details:\n")
        for user_id in sorted(users):
            user_path = os.path.join(dataset_path, str(user_id))
            info_path = os.path.join(user_path, "info.txt")
            if os.path.exists(info_path):
                with open(info_path, 'r') as info_file:
                    f.write(f"User {user_id}:\n")
                    for line in info_file:
                        f.write(f"  {line.strip()}\n")
            else:
                f.write(f"User {user_id}: No additional information\n")

def train_model():
    """
    Trains the face recognition model with improved parameters.

    This function:
    1. Finds all the face photos you've taken
    2. Processes each photo to make it easier to recognize
    3. Teaches the computer to recognize each person
    4. Saves the trained model so it can be used later

    Returns:
        bool: True if training was successful, False otherwise
    """
    # Setup paths and check required files
    paths = setup_paths()
    if paths is None:
        return False

    try:
        recognizer = cv2.face.LBPHFaceRecognizer_create(
            radius=2,
            neighbors=8,
            grid_x=8,
            grid_y=8,
            threshold=100
        )

        detector = cv2.CascadeClassifier(paths["cascade_path"])

        print("Training face recognition model with improved parameters...")
        print("This may take a few minutes...")

        faces, ids = get_images_and_labels(paths["dataset_path"], detector)

        if len(faces) == 0 or len(ids) == 0:
            print("Error: No face samples found. Please capture photos first.")
            return False

        print(f"Training with {len(faces)} face samples")

        recognizer.train(faces, np.array(ids))

        model_path = os.path.join(paths["trainer_path"], 'trainer.yml')
        recognizer.write(model_path)

        print(f"Model trained successfully with {len(faces)} face samples")
        print(f"Model saved to {model_path}")

        users = set(ids)
        save_user_details(paths["trainer_path"], paths["dataset_path"], users, len(faces))

        return True

    except Exception as e:
        print(f"Error during training: {str(e)}")
        return False

if __name__ == "__main__":
    train_model()
