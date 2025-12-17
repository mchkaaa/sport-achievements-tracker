package models;

public class Training {
    private int id;
    private String date;
    private String type;
    private int duration;
    private int mood; // 1..5
    private String note;

    public Training(int id, String date, String type, int duration, int mood, String note) {
        this.id = id;
        this.date = date;
        this.type = type;
        this.duration = duration;
        this.mood = mood;
        this.note = note;
    }

    public int getId() { return id; }
    public String getDate() { return date; }
    public String getType() { return type; }
    public int getDuration() { return duration; }
    public int getMood() { return mood; }
    public String getNote() { return note; }

    // Для відображення emoji
    public String getMoodEmoji() {
        switch (mood) {
            case 1: return "😫";
            case 2: return "😕";
            case 3: return "😐";
            case 4: return "🙂";
            case 5: return "🤩";
            default: return "";
        }
    }
}
