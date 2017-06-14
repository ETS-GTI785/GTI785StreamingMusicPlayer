package tonyd.musicplayergti785;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tonyd on 5/28/2017.
 */

public class RequestAsyncTask extends AsyncTask<String, Void, String> {

    private String baseUrl;

    public LocalBroadcastManager getBroadcaster() {
        return broadcaster;
    }

    private LocalBroadcastManager broadcaster;
    private Context context;

    public RequestAsyncTask(Context context, String ipAddress, String port) {
        this.context = context;
        broadcaster = LocalBroadcastManager.getInstance(context);
        baseUrl = "http://" + ipAddress + ":" + port + "/";
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            String command = params[0];
            URL mergedURL = URLbuilt(params);

            HttpURLConnection conn = (HttpURLConnection) mergedURL.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(500);
            String method = GetRequestMethod(command);
            conn.setRequestMethod(method);
            int responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                if (command.equals(Command.COVER)) {
                    byte[] bytes = IOUtils.toByteArray(conn.getInputStream());
                    updateCover(bytes);
                } else {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    String inputLine;

                    if (command.equals(Command.PLAYLIST)) {
                        Gson gson = new Gson();
                        Type listType = new TypeToken<ArrayList<PlaylistResponse>>() {
                        }.getType();
                        List<PlaylistResponse> listPlaylistResponse = gson.fromJson(in, listType);
                        System.out.println("Library retrieved");
                        updateLibrary(listPlaylistResponse);
                    }

                    if (command.equals(Command.STATUS)) {
                        Gson gson = new Gson();
                        StatusResponse sr = gson.fromJson(in, StatusResponse.class);
                        updateStatus(sr);
                    }

                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    return "SUCCESS";
                }
            }
            return "SUCCESS";
        } catch (Exception e) {
            return "Exception: " + e.getMessage();
        }
    }

    private URL URLbuilt(String[] params) throws MalformedURLException {
        String command = params[0];
        int seekTo = -1;
        String filename = "";
        if (command.equals(Command.SEEK) && params.length == 2) {
            command = params[0];
            seekTo = Integer.parseInt(params[1]);
        }
        if (command.equals(Command.COVER) && params.length == 2) {
            command = params[0];
            filename = params[1];
        }
        URL url = new URL(baseUrl);
        String relURL;
        if (command == Command.SEEK) {
            relURL = "/" + command + "/" + seekTo;
        } else if (command.equals(Command.COVER)) {
            relURL = "/" + command + "/" + filename;
        } else {
            relURL = "/" + command;
        }
        return new URL(url, relURL);
    }

    private void updateCover(byte[] bytes) {
        Intent intent = new Intent(Command.COMMAND);
        intent.putExtra(Command.COMMAND, Command.COVER);
        intent.putExtra("image", bytes);
        broadcaster.sendBroadcast(intent);
    }

    private String GetRequestMethod(String command) {
        switch (command) {
            case Command.PLAY:
                return "POST";
            case Command.PAUSE:
                return "POST";
            case Command.STOP:
                return "POST";
            case Command.NEXT:
                return "POST";
            case Command.PREVIOUS:
                return "POST";
            case Command.SHUFFLE:
                return "POST";
            case Command.REPEAT:
                return "POST";
            case Command.SEEK:
                return "POST";
            case Command.PLAYLIST:
                return "GET";
            case Command.STATUS:
                return "GET";
            case Command.SELECT:
                return "POST";
            case Command.STREAM:
                return "GET";
            default:
                return "GET";
        }
    }

    private void updateLibrary(List<PlaylistResponse> listPlaylistResponse) {
        Library library = Library.getInstance();
        library.init();
        for (int i = 0; i < listPlaylistResponse.size(); i++) {
            PlaylistResponse pr = listPlaylistResponse.get(i);
            Song song = new Song(pr.getFilename(), pr.getTitle(), pr.getArtist(), pr.getAlbum(), pr.getYear(), pr.getDuration());
            library.addOriginalSong(song);
            library.addSong(song);
        }
        Intent intent = new Intent(Command.COMMAND);
        intent.putExtra(Command.COMMAND, Command.PLAYLIST);
        broadcaster.sendBroadcast(intent);
    }

    private void updateStatus(StatusResponse sr) {
        Intent intent = new Intent(Command.COMMAND);
        intent.putExtra(Command.COMMAND, Command.STATUS);
        intent.putExtra("status", sr);
        broadcaster.sendBroadcast(intent);
    }

}

