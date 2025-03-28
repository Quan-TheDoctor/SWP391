from flask import Flask, request, jsonify, send_from_directory
import subprocess
import threading
import os
import time
import uuid
import json
from werkzeug.utils import secure_filename
from logger_config import setup_logger

from parser import ResumeParser

logger = setup_logger('resume_api')

os.environ['PYTHONUNBUFFERED'] = '1'

app = Flask(__name__)

current_process = None
system_status = {
    "status": "idle",
    "message": "System is idle",
    "last_updated": time.time()
}

resume_cache = {}

UPLOAD_FOLDER = 'uploads'
ALLOWED_EXTENSIONS = {'pdf', 'docx'}
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

os.makedirs(UPLOAD_FOLDER, exist_ok=True)
os.makedirs("datasets", exist_ok=True)
os.makedirs("trainer", exist_ok=True)


def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS


@app.route('/parse_resume', methods=['POST'])
def parse_resume():
    logger.info("Received resume parsing request")
    if 'file' not in request.files:
        logger.error("No file part in the request")
        return jsonify({'error': 'No file part'}), 400
    
    file = request.files['file']

    if file.filename == '':
        logger.error("No selected file")
        return jsonify({'error': 'No selected file'}), 400
    
    if file and allowed_file(file.filename):
        original_filename = secure_filename(file.filename)
        file_extension = original_filename.rsplit('.', 1)[1].lower()
        unique_filename = f"{uuid.uuid4().hex}.{file_extension}"
        file_path = os.path.join(app.config['UPLOAD_FOLDER'], unique_filename)
        file.save(file_path)
        
        try:
            # Parse the resume
            parser = ResumeParser()
            result = parser.parse(file_path)
            
            # Clean up the uploaded file after processing
            os.remove(file_path)
            
            logger.info(f"Successfully parsed resume: {original_filename}")
            
            # Ensure all data is JSON serializable
            for key, value in result.items():
                if isinstance(value, set):
                    result[key] = list(value)
            
            return jsonify(result)
        except Exception as e:
            logger.error(f"Error parsing resume: {str(e)}")
            # Clean up on error
            if os.path.exists(file_path):
                os.remove(file_path)
            return jsonify({'error': str(e)}), 500
    
    logger.error(f"File type not allowed: {file.filename}")
    return jsonify({'error': 'File type not allowed'}), 400

@app.route('/resume_cache/<cache_id>', methods=['GET'])
def get_cached_resume(cache_id):
    """Retrieve a previously parsed resume from cache"""
    if cache_id in resume_cache:
        return jsonify(resume_cache[cache_id]['parsed_data'])
    else:
        return jsonify({'error': 'Resume not found in cache'}), 404


# Clean up old cache entries periodically
def cleanup_cache():
    """Remove cache entries older than 1 hour"""
    current_time = time.time()
    expired_keys = []
    
    for key, data in resume_cache.items():
        if current_time - data['timestamp'] > 3600:  # 1 hour
            expired_keys.append(key)
    
    for key in expired_keys:
        del resume_cache[key]


@app.route('/api/status', methods=['GET'])
def get_status():
    logger.info("Received resume parsing request")
    """Get the current status of the face recognition system"""
    return jsonify(system_status)

@app.route('/api/take-photos', methods=['POST'])
def take_photos():
    logger.info("Received photo capture request")
    """Start the photo capture process for a user"""
    global current_process, system_status

    if current_process and current_process.poll() is None:
        logger.info("Terminating existing process")
        current_process.terminate()
        current_process = None

    try:
        data = request.json
        user_id = data.get('id')
        user_name = data.get('name', '')

        logger.info(f"Starting photo capture for user_id: {user_id}, name: {user_name}")

        if not user_id:
            logger.error("Missing user ID in request")
            return jsonify({
                'status': 'error',
                'message': 'User ID is required'
            })

        system_status = {
            "status": "running",
            "message": f"Taking photos for user {user_id}",
            "last_updated": time.time()
        }

        # Start the process in a separate thread to avoid blocking
        def run_process():
            global current_process, system_status
            try:
                cmd = ['py', 'take_photo.py', str(user_id)]
                if user_name:
                    cmd.append(user_name)

                logger.info(f"Executing command: {' '.join(cmd)}")

                current_process = subprocess.Popen(cmd,
                                                   stdout=subprocess.PIPE,
                                                   stderr=subprocess.PIPE)
                stdout, stderr = current_process.communicate()

                logger.info(f"Process completed with return code: {current_process.returncode}")
                if stdout:
                    logger.info(f"Process stdout: {stdout.decode('utf-8')}")
                if stderr:
                    logger.error(f"Process stderr: {stderr.decode('utf-8')}")

                if current_process.returncode == 0:
                    system_status = {
                        "status": "success",
                        "message": f"Photo capture completed for user {user_id}",
                        "last_updated": time.time()
                    }
                else:
                    system_status = {
                        "status": "error",
                        "message": f"Photo capture failed for user {user_id}",
                        "last_updated": time.time()
                    }
            except Exception as e:
                logger.error(f"Exception during photo capture: {str(e)}")
                system_status = {
                    "status": "error",
                    "message": f"Error: {str(e)}",
                    "last_updated": time.time()
                }

        threading.Thread(target=run_process).start()

        return jsonify({
            'status': 'success',
            'message': f'Started photo capture for user {user_id}'
        })

    except Exception as e:
        logger.error(f"Exception in take_photos endpoint: {str(e)}")
        system_status = {
            "status": "error",
            "message": f"Error: {str(e)}",
            "last_updated": time.time()
        }
        return jsonify({
            'status': 'error',
            'message': f'Failed to start photo capture: {str(e)}'
        })


@app.route('/api/train', methods=['POST'])
def train_model():
    logger.info("Received resume parsing request")
    """Train the face recognition model"""
    global current_process, system_status

    if current_process and current_process.poll() is None:
        current_process.terminate()
        current_process = None

    try:
        system_status = {
            "status": "running",
            "message": "Training face recognition model...",
            "last_updated": time.time()
        }

        def run_training():
            global current_process, system_status
            try:
                current_process = subprocess.Popen(['python', 'train_model.py'])
                current_process.wait()

                if current_process.returncode == 0:
                    system_status = {
                        "status": "success",
                        "message": "Model training completed successfully",
                        "last_updated": time.time()
                    }
                else:
                    system_status = {
                        "status": "error",
                        "message": "Model training failed",
                        "last_updated": time.time()
                    }
            except Exception as e:
                system_status = {
                    "status": "error",
                    "message": f"Training error: {str(e)}",
                    "last_updated": time.time()
                }

        threading.Thread(target=run_training).start()

        return jsonify({
            'status': 'success',
            'message': 'Started model training process'
        })

    except Exception as e:
        system_status = {
            "status": "error",
            "message": f"Error: {str(e)}",
            "last_updated": time.time()
        }
        return jsonify({
            'status': 'error',
            'message': f'Failed to start training: {str(e)}'
        })

@app.route('/api/recognize', methods=['POST'])
def recognize():
    logger.info("Received resume parsing request")
    global current_process, system_status
    type_param = request.args.get('type', 'default')

    if current_process and current_process.poll() is None:
        current_process.terminate()
        current_process = None

    try:
        system_status = {
            "status": "running",
            "message": "Face recognition is active",
            "last_updated": time.time()
        }

        def run_recognition():
            global current_process, system_status
            try:
                current_process = subprocess.Popen(['python', 'face_recognition.py', type_param])
                current_process.wait()

                if current_process.returncode == 0:
                    system_status = {
                        "status": "success",
                        "message": "Face recognition completed",
                        "last_updated": time.time()
                    }
                else:
                    system_status = {
                        "status": "error",
                        "message": "Face recognition process failed",
                        "last_updated": time.time()
                    }
            except Exception as e:
                system_status = {
                    "status": "error",
                    "message": f"Recognition error: {str(e)}",
                    "last_updated": time.time()
                }

        threading.Thread(target=run_recognition).start()

        return jsonify({
            'status': 'success',
            'message': 'Started face recognition process'
        })

    except Exception as e:
        system_status = {
            "status": "error",
            "message": f"Error: {str(e)}",
            "last_updated": time.time()
        }
        return jsonify({
            'status': 'error',
            'message': f'Failed to start recognition: {str(e)}'
        })

@app.route('/api/stop', methods=['POST'])
def stop_process():
    logger.info("Received resume parsing request")
    global current_process, system_status

    try:
        if current_process and current_process.poll() is None:
            current_process.terminate()
            current_process = None

            system_status = {
                "status": "idle",
                "message": "Process stopped successfully",
                "last_updated": time.time()
            }

            return jsonify({
                'status': 'success',
                'message': 'Process stopped successfully'
            })
        else:
            system_status = {
                "status": "idle",
                "message": "No active process to stop",
                "last_updated": time.time()
            }

            return jsonify({
                'status': 'success',
                'message': 'No active process to stop'
            })

    except Exception as e:
        system_status = {
            "status": "error",
            "message": f"Error stopping process: {str(e)}",
            "last_updated": time.time()
        }

        return jsonify({
            'status': 'error',
            'message': f'Failed to stop process: {str(e)}'
        })

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)