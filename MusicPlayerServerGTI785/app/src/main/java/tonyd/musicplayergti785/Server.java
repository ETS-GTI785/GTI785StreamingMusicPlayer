package tonyd.musicplayergti785;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaMetadataRetriever;

import com.google.gson.Gson;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.request.Method;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tonyd on 5/25/2017.
 */

public class Server extends NanoHTTPD {

    private static final String ipadress = "10.0.2.15";
    private static final int port = 7000;

    private WebService service;
    private Context context;

    public Server(WebService wb, Context context) {
        super(ipadress, port);
        service = wb;
        this.context = context;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();
        processRequestForMainActivity(uri); // Send broadcast message to MainActivity
        return buildResponseForClient(uri); // Send back the appropriate response to the client
    }

    private Response buildResponseForClient(String uri) {
        String[] separated = mySplit(uri, "/");
        String command = separated[0];
        Gson gson = new Gson();
        switch (command) {
            case Command.PLAY:
                return Response.newFixedLengthResponse(Command.OK);
            case Command.PAUSE:
                return Response.newFixedLengthResponse(Command.OK);
            case Command.STOP:
                return Response.newFixedLengthResponse(Command.OK);
            case Command.NEXT:
                return Response.newFixedLengthResponse(Command.OK);
            case Command.PREVIOUS:
                return Response.newFixedLengthResponse(Command.OK);
            case Command.SHUFFLE:
                return Response.newFixedLengthResponse(Command.OK);
            case Command.REPEAT:
                return Response.newFixedLengthResponse(Command.OK);
            case Command.SEEK:
                return Response.newFixedLengthResponse(Command.OK);
            case Command.PLAYLIST:
                List<Song> songs = Library.getInstance().getCurrentPlaylist();
                List<PlaylistResponse> listPlaylistResponse = new ArrayList<>();
                for (int i = 0; i < songs.size(); i++) {
                    PlaylistResponse pr = new PlaylistResponse(
                            songs.get(i).getFilename(),
                            songs.get(i).getName(),
                            songs.get(i).getArtist().getName(),
                            songs.get(i).getAlbum().getName(),
                            songs.get(i).getYear(),
                            songs.get(i).getDuration()
                    );
                    listPlaylistResponse.add(pr);
                }
                String serializedList = gson.toJson(listPlaylistResponse);
                return Response.newFixedLengthResponse(serializedList);
            case Command.STATUS:
                Song song = Library.getInstance().getCurrentSong();
                StatusResponse sr = new StatusResponse(
                        song.getFilename(),
                        song.getName(),
                        song.getArtist().getName(),
                        song.getAlbum().getName(),
                        "2000",
                        Library.getInstance().isPlaying(),
                        Library.getInstance().isRepeatMode(),
                        Library.getInstance().getCurrentPosition(),
                        song.getDuration()

                );
                String serializedSong = gson.toJson(sr);
                return Response.newFixedLengthResponse(serializedSong);
            case Command.SELECT:
                return Response.newFixedLengthResponse(Command.OK);
            case Command.STREAM:
                String file = separated[1] + "/" + separated[2];
                AssetFileDescriptor fileDescriptor;
                FileInputStream stream;
                try {
                    fileDescriptor = context.getAssets().openFd(file);
                    stream = fileDescriptor.createInputStream();
                    return Response.newChunkedResponse(Status.OK, "audio/mpeg", stream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Command.COVER:
                MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
                String fileCover = separated[1] + "/" + separated[2];
                AssetFileDescriptor fdCover;
                try {
                    fdCover = context.getAssets().openFd(fileCover);
                    metaRetriever.setDataSource(fdCover.getFileDescriptor(), fdCover.getStartOffset(), fdCover.getLength());
                    byte[] image = metaRetriever.getEmbeddedPicture();
                    return Response.newFixedLengthResponse(Status.OK, "image/jpeg", image);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                return Response.newFixedLengthResponse(Command.UNKNOWN);
        }
        return null;
    }

    private void processRequestForMainActivity(String uri) {
        String[] separated = mySplit(uri, "/");
        String command = separated[0];
        Intent intent = new Intent(Command.COMMAND);
        intent.putExtra(Command.COMMAND, command);
        if (command.equals(Command.SEEK)) {
            intent.putExtra(Command.SEEKVALUE, Integer.parseInt(separated[1]));
        }
        service.getBroadcaster().sendBroadcast(intent);
    }

    /* Utilities */
    public String[] mySplit(final String input, final String delim) {
        return input.replaceFirst("^" + delim, "").split(delim);
    }
}
