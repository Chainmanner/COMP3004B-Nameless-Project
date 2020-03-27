package project.comp3004.hyggelig.help.HelpDB;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TopicDao {
    @Query("SELECT * FROM Topics WHERE topicID=:topicID")
    Topic getTopic(String topicID);
}
