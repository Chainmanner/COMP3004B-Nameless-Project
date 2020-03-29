package project.comp3004.hyggelig.help.HelpListFrag;

import java.util.ArrayList;
import java.util.List;

import project.comp3004.hyggelig.help.HelpDB.TopicList;

/**
 * Helper class for providing the help topics to the HelpListFragment
 * Though it currently just provides a List of Strings that show the topics,
 * keeping the format close to the template's "DummyContent" allows for easy extensibility.
 *
 * E.g. instead of opening a new fragment, the help topic's content could be part of this class
 * and shown as a pop-up
 *
 */
public class HelpTopicListContent {

    /**
     * A list of the HelpTopics accessible by the Adapter
     */
    public static final List<HelpTopic> ITEMS = new ArrayList<HelpTopic>();


    protected static void setTopics(TopicList[] topicList){
        ITEMS.clear();
        for(TopicList topic: topicList){
            addTopic(createHelpTopic(topic));
        }
    }

    protected static void addTopic(HelpTopic topic) {
        ITEMS.add(topic);
    }

    private static HelpTopic createHelpTopic(TopicList topic) {
        return new HelpTopic(topic);
    }

    /**
     * A class representing a help topic
     * Contains the help topic, the category of the topic, and the topicID (from the database)
     */
    public static class HelpTopic {
        public final String topic;
        public final String category;
        public final int topicID;

        public HelpTopic(TopicList topic) {
            this.topic = topic.topic;
            this.topicID = topic.topicID;
            this.category = topic.category;
        }

        @Override
        public String toString() {
            return topic;
        }
    }
}
