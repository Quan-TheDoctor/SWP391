from flask import Flask, request, jsonify
import subprocess
import threading
import os
import time

app = Flask(__name__)

# Global process tracker
current_process = None
system_status = {
    "status": "idle",
    "message": "System is idle",
    "last_updated": time.time()
}

# Ensure directories exist
os.makedirs("datasets", exist_ok=True)
os.makedirs("trainer", exist_ok=True)

@app.route('/api/status', methods=['GET'])
def get_status():
    """Get the current status of the face recognition system"""
    return jsonify(system_status)

@app.route('/api/take-photos', methods=['POST'])
def take_photos():
    """Start the photo capture process for a user"""
    global current_process, system_status

    # Stop any existing process
    if current_process and current_process.poll() is None:
        current_process.terminate()
        current_process = None

    try:
        data = request.json
        user_id = data.get('id')
        user_name = data.get('name', '')

        if not user_id:
            return jsonify({
                'status': 'error',
                'message': 'User ID is required'
            })

        # Update system status
        system_status = {
            "status": "running",
            "message": f"Taking photos for user {user_id}",
            "last_updated": time.time()
        }

        # Start the process in a separate thread to avoid blocking
        def run_process():
            global current_process, system_status
            try:
                cmd = ['python', 'take_photo.py', str(user_id)]
                if user_name:
                    cmd.append(user_name)

                current_process = subprocess.Popen(cmd)
                current_process.wait()

                # Update status when complete
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
    """Train the face recognition model"""
    global current_process, system_status

    # Stop any existing process
    if current_process and current_process.poll() is None:
        current_process.terminate()
        current_process = None

    try:
        # Update system status
        system_status = {
            "status": "running",
            "message": "Training face recognition model...",
            "last_updated": time.time()
        }

        # Start the training process in a separate thread
        def run_training():
            global current_process, system_status
            try:
                current_process = subprocess.Popen(['python', 'train_model.py'])
                current_process.wait()

                # Update status when complete
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
    global current_process, system_status

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
                current_process = subprocess.Popen(['python', 'face_recognition.py'])
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
    app.run(host='0.0.0.0', port=5000, debug=True)
