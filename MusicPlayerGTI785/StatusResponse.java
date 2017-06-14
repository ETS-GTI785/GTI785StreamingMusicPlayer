public class StatusResponse{
    private String filename;
    private String title;
    private String artist;
    private String album;
    private boolean playing;
    private boolean repeating;
    private int currentPosition;
    private int duration;

    public StatusResponse(filename, title, artist, album, playing, repeating, currentPosition, duration){
        this.filename = filename;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.playing = playing;
        this.repeating = repeating;
        this.currentPosition = currentPosition;
        this.duration = duration;
    }
}