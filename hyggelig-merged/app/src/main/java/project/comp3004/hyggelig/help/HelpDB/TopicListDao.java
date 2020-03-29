package project.comp3004.hyggelig.help.HelpDB;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface TopicListDao {
        @Query("SELECT * from TopicLists WHERE category=:category")
        public TopicList[] getTopics(String category);
        @Query("SELECT * FROM TopicLists GROUP BY category")
        public TopicList[] getCategories();
}
