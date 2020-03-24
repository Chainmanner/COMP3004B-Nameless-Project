package project.comp3004.hyggelig.help.HelpDB;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "TopicLists")
public class TopicList {
    @PrimaryKey@NonNull
    public String topic;

    @ColumnInfo(name = "topicID")@NonNull
    public int topicID;

    @ColumnInfo(name = "category")@NonNull
    public String category;

}
