package project.comp3004.hyggelig.help.HelpDB;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Topics")
public class Topic {
    @PrimaryKey@NonNull
    public int topicID;

    @ColumnInfo(name = "help_text")@NonNull
    public String helpText;
}
