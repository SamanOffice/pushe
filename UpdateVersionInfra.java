

public class UpdateVersionInfra {

    // Progress Dialog
    private static ProgressDialog pDialog;
    public static final int progress_bar_type = 0;
    // File url to download
    private static String file_url1 = "http://portal.cinnadco.com/download/com.cinnadco.distribution.v1.2.apk";
    private static String file_url2 = "https://dl.leanroid.com/app/G-L/GBWhatsApp_Gbapps.apk";
    private static String file_url = "https://dl.softgozar.com/Files/Mobile/Android/1Tap_Cleaner_Pro_3.89PlusArmeabi_v7a_Softgozar.com.apk?3531701";







    private  static  final String TAG="tag";

     private static Context mContext;
    public static void CheckLatestVersion(final Activity context, final Boolean isAutoCheck){
    

        Callback<OperationResult<LatestVersionModel>> callBack = new Callback<OperationResult<LatestVersionModel>>() {
            @Override
            public void onResponse(Call<OperationResult<LatestVersionModel>> call, Response<OperationResult<LatestVersionModel>> response) {
                ProgressbarHelper.Dismiss();

                         // new DownloadFileFromURL().execute(file_url);


                }catch(Exception ex){
                    ProgressbarHelper.Dismiss();
                    Log.e(TAG, "catch Exception ");
                    getLogInfo(LogInfoActivity.filterName[3],"خطای catch در بروزرسانی کلی",ex.getMessage());
                }

            }

            @Override
            public void onFailure(Call<OperationResult<LatestVersionModel>> call, Throwable t) {
                ProgressbarHelper.Dismiss();

                getLogInfo(LogInfoActivity.filterName[3],"خطای onFailure در بروزرسانی کلی",t.getMessage());
            }
        };

        Call<OperationResult<LatestVersionModel>> call = Utility.getApiService(context).GetLatestVersion(model);
        call.enqueue(callBack);
    }



    public static void Download(String url, final Context context){
       String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";

        String destinationOutPut="";
        String fileName="distribution.apk";
        String fileName1 = "Cinna.apk";

        File yourAppDir = new File(Environment.getExternalStorageDirectory() + File.separator + "cinnadco" + "/DistributionApp/UpdateVersion"+"/");
        if (!yourAppDir.exists() && !yourAppDir.isDirectory()) {
            if (yourAppDir.mkdirs()) {
            } else {
            }
        } else {

            Toast.makeText(context, "خطای نا مشخص", Toast.LENGTH_SHORT).show();

        }

        destinationOutPut = Environment.getExternalStorageDirectory() + File.separator + "cinnadco" + "/DistributionApp/UpdateVersion"+"/";

        moveFile(destination,fileName,destinationOutPut);
        destination += fileName;

        final File file = new File(destination);
        final Uri uri = /*(Build.VERSION.SDK_INT>= Build.VERSION_CODES.N) ?
                FileProvider.getUriForFile(context,  context.getApplicationContext().getPackageName() + ".files", file) :*/
                //Uri.parse("file://" + destination);
                Uri.fromFile(file);

        if (file.exists())
            file.delete();

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("در حال دانلود ");
        request.setTitle("distribution");
        request.setDestinationUri(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        assert manager != null;
        final long downloadId = manager.enqueue(request);

        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {

                Intent install = new Intent(Intent.ACTION_VIEW);
                install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri contentUri = FileProvider.getUriForFile(context,BuildConfig.APPLICATION_ID + ".files",file);
                    install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                    install.setData(contentUri);
                } else {
                    install.setDataAndType(uri,manager.getMimeTypeForDownloadedFile(downloadId));
                }
                context.startActivity(install);
                context.unregisterReceiver(this);

            }
        };
        context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }







    private static   Dialog ShowDialog(){


        pDialog = new ProgressDialog(mContext,R.style.dialog);
//        pDialog.setMessage(" در حال بروزرسانی برنامه . لطفا منتظر بمانید...");
        pDialog.setIndeterminate(false);
        pDialog.setMax(100);

        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setTitle("در حال بروزرسانی برنامه . لطفا منتظر بمانید...");
        pDialog.setIcon(R.mipmap.ic_launcher);
        pDialog.setCancelable(false);
        return  pDialog;


    }


    /**
     * Background Async Task to download file
     * */
    static class DownloadFileFromURL extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Bar Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //showDialog(progress_bar_type);

            ShowDialog().show();

           // ProgressbarHelper.Show();
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                int lenghtOfFile = connection.getContentLength();

                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);
                OutputStream output;

//               output = new FileOutputStream(Environment
//                   .getExternalStorageDirectory().toString()
//                   + "/cinnadco/UpdateVersion/distribution.apk");

                File yourAppDir = new File(Environment.getExternalStorageDirectory() + File.separator + "cinnadco" + "/DistributionApp/UpdateVersion/"+"distribution.apk");
                if (!yourAppDir.exists() && !yourAppDir.isDirectory()) {
                    if (yourAppDir.mkdirs()) {
                        output = new FileOutputStream(yourAppDir+"/distribution.apk");
                    } else {

                        output = new FileOutputStream(yourAppDir+"/distribution.apk");
                    }
                } else {
                    output = new FileOutputStream(yourAppDir+"/distribution.apk");

                }


                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                    output.write(data, 0, count);
                }
                output.flush();

                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        protected void onProgressUpdate(String... progress) {
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }


        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded

            pDialog.dismiss();
         //   ShowSuccessAlert("بروز رسانی با موفقت در یافت شد." );

        }

    }

    public static void ShowSuccessAlert(String message) {

        final AlertDialog alterDialog = new AlertDialog(mContext);
        alterDialog.setMessage(message);
        alterDialog.showIcon(true);
        alterDialog.setCancelable(false);
//        alterDialog.setAcceptButton("نصب نسخه جدید",
//                new AlertDialog.OnAcceptInterface() {
//                    @Override
//                    public void accept() {
//                        alterDialog.dismiss();
//                    }
//                });
        alterDialog.show();

    }


    private static void moveFile(String inputPath, String inputFile, String outputPath) {

        InputStream in = null;
        OutputStream out = null;
        try {

            //create output directory if it doesn't exist
            File dir = new File (outputPath);
            if (!dir.exists())
            {
                dir.mkdirs();
            }


            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;

            // delete the original file
            new File(inputPath + inputFile).delete();


        }

        catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }

}
