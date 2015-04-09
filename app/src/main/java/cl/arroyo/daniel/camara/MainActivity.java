package cl.arroyo.daniel.camara;

import java.io.File;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;


import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    Button btnCamara,btnHora;
    private String foto;
    private static int TAKE_PICTURE = 1;
    double aleatorio = 0;

    private NotificationManager mNotifyManager;
    private Builder mBuilder;
    int id = 1;

    MetodosCamara metodos = new MetodosCamara();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Para crear un nombre diferente para la foto
        aleatorio = new Double(Math.random() * 100).intValue();
        foto = Environment.getExternalStorageDirectory() + "/imagen"+ aleatorio +".jpg";

		/*
		 * Le damos vida al boton y al presionarlo ejecutamos la camara
		 */
        btnCamara = (Button) findViewById(R.id.button1);
        btnCamara.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				/*
				 * Creamos un fichero donde guardaremos la foto
				 */
                Uri output = Uri.fromFile(new File(foto));
                intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
				/*
				 * Lanzamos el intenta y recogemos el resultado en onActivityResult
				 */
                startActivityForResult(intent, TAKE_PICTURE); // 1 para la camara, 2 para la galeria

                mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mBuilder = new NotificationCompat.Builder(MainActivity.this);
                mBuilder.setContentTitle("Cargando")
                        .setContentText("Carga en progreso")
                        .setSmallIcon(R.drawable.abc_list_focused_holo);
            }
        });
        btnHora = (Button) findViewById(R.id.mostrarHora);
        btnHora.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v){
                Toast.makeText(getApplicationContext(),metodos.fechaCompleta(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        ImageView iv = (ImageView) findViewById(R.id.imageView1);
        iv.setImageBitmap(BitmapFactory.decodeFile(foto));

        File file = new File(foto);
        if (file.exists()) {
            UploaderFoto nuevaTarea = new UploaderFoto();
            nuevaTarea.execute(foto);
        }
        else
            Toast.makeText(getApplicationContext(), "No se ha realizado la foto", Toast.LENGTH_SHORT).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    /*
	 * Clase asincrona para subir la foto
	 */
    class UploaderFoto extends AsyncTask<String, Integer, Void>{

        ProgressDialog pDialog;
        String miFoto = "";

        @Override
        protected Void doInBackground(String... params) {
            miFoto = params[0];
            try {

                int i;
                for (i = 0; i <= 100; i += 5) {
                    // Sets the progress indicator completion percentage
                    publishProgress(Math.min(i, 100));
                    try {
                        // Sleep for 5 seconds
                        Thread.sleep(2 * 1000);
                    } catch (InterruptedException e) {
                        Log.d("TAG", "sleep failure");
                    }
                }

                HttpClient httpclient = new DefaultHttpClient();
                httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
                HttpPost httppost = new HttpPost("http://camara.lerolero.cl/uploads/up.php");
                File file = new File(miFoto);
                MultipartEntity mpEntity = new MultipartEntity();
                ContentBody foto = new FileBody(file, "image/jpeg");
                mpEntity.addPart("fotoUp", foto);
                httppost.setEntity(mpEntity);
                httpclient.execute(httppost);
                httpclient.getConnectionManager().shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            // Update progress
            mBuilder.setProgress(100, values[0], false);
            mNotifyManager.notify(id, mBuilder.build());
            super.onProgressUpdate(values);
        }

        protected void onPreExecute() {
            super.onPreExecute();

            // Displays the progress bar for the first time.
            mBuilder.setProgress(100, 0, false);
            mNotifyManager.notify(id, mBuilder.build());

            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Subiendo la imagen, espere." );
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setCancelable(true);
            pDialog.show();
        }
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pDialog.dismiss();

            mBuilder.setContentText("Carga completada");
            // Removes the progress bar
            mBuilder.setProgress(0, 0, false);
            mNotifyManager.notify(id, mBuilder.build());
        }
    }
}
