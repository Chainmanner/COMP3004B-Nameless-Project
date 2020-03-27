// FIXME: Output images are rotated 90 degrees CCW and I don't know how to fix that.

package project.comp3004.hyggelig.camera;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;

import project.comp3004.hyggelig.R;

// Activity for the user to take a picture or record a video, then exit.
// MUST be called from another activity. Only used in EncryptFilesFragment.
// Authored by Gabriel Valachi (101068875).
public class CameraActivity extends AppCompatActivity
{
	private Camera theCamera;
	private CameraPreview thePreview;

	private String outPath;
	private boolean recordVideo;	// true = record video instead of taking a picture

	private Camera.PictureCallback thePictureCallback;

	private MediaRecorder theRecorder;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_preview);

		// Open the camera for usage.
		theCamera = getCamera();

		// Create the preview.
		thePreview = new CameraPreview(this, theCamera);
		FrameLayout previewFrame = this.findViewById(R.id.camera_preview);
		previewFrame.addView(thePreview);

		// Get parameters for which this activity was called.
		outPath = "";
		recordVideo = false;
		Bundle params = this.getIntent().getExtras();
		if ( params != null )
		{
			outPath = params.getString("outPath");
			recordVideo = params.getBoolean("recordVideo");
		}

		// Callback for when a picture is taken.
		thePictureCallback = new Camera.PictureCallback() {
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				Log.w("hyggelig", "onPictureTaken");
				saveData(data);
				setResult(RESULT_OK);
				finish();
			}
		};

		theRecorder = null;	// Needed for recording videos.

		// Set the capture button's click listener.
		final Button button_capture = this.findViewById(R.id.button_capture);
		if ( recordVideo )	// Video
		{
			// For video, have the button start the recording initially, then stop it when clicked again.
			// This wouldn't be ideal for a normal camera application, but we're exiting when the recording's stopped, so that's fine.
			button_capture.setText("Start");
			button_capture.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					button_capture.setText("Stop");
					button_capture.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							stopRecording();
						}
					});
					startRecording();
				}
			});
		}
		else			// Picture
		{
			// Just take a picture on clicking the button.
			button_capture.setText("Capture");
			button_capture.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					theCamera.takePicture(null, null, thePictureCallback);
				}
			});
		}
	}

	@Override
	public void onStart()
	{
		Log.w("hyggelig", "CameraActivity.onStart");
		super.onStart();
		// Nothing to do here.
	}

	@Override
	public void onStop()
	{
		Log.w("hyggelig", "CameraActivity.onStop");
		super.onStop();

		// Release the camera resources so that other programs can use the camera.
		theCamera.stopPreview();
		theCamera.release();
	}

	// Safely get the Camera instance.
	private static Camera getCamera()
	{
		Camera retCamera = null;
		try
		{
			retCamera = Camera.open();
		}
		catch ( Exception e )
		{
			Log.w("hyggelig", "Error while attempting to open camera: " + e.getMessage());
			e.printStackTrace();
		}
		return retCamera;
	}

	// Prepare the camera and video output, then start the recording.
	private void startRecording()
	{
		theRecorder = new MediaRecorder();

		theCamera.unlock();
		theRecorder.setCamera(theCamera);

		theRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		theRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);

		theRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));

		theRecorder.setOutputFile(outPath);

		theRecorder.setPreviewDisplay(thePreview.getHolder().getSurface());

		try
		{
			theRecorder.prepare();
		}
		catch ( Exception e )
		{
			Log.w("hyggelig", "Error while starting to record: " + e.getMessage());
			e.printStackTrace();
			theRecorder.release();
			return;
		}

		theRecorder.start();
	}

	// Stop the recording, release used resources, and terminate this CameraActivity with a successful result.
	private void stopRecording()
	{
		if ( theRecorder == null )
			return;

		theRecorder.stop();

		theRecorder.reset();

		theRecorder.release();

		theCamera.lock();
		//theCamera.stopPreview();
		//theCamera.release();
		setResult(RESULT_OK);
		finish();
	}

	// Save the input data to the file indicated by outPath.
	private void saveData(byte[] data)
	{
		File outFile = new File(outPath);

		try
		{
			outFile.createNewFile();

			FileOutputStream fileOS = new FileOutputStream(outFile);
			fileOS.write(data);
			fileOS.close();
		}
		catch ( Exception e )
		{
			Log.w("hyggelig", "Error while attempting to save to " + outPath + ": " + e.getMessage());
			e.printStackTrace();
		}
	}
}
