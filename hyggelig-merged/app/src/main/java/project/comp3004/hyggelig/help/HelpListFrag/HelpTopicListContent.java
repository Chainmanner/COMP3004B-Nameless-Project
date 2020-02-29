package project.comp3004.hyggelig.help.HelpListFrag;

import java.util.ArrayList;
import java.util.List;

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


    protected static void setTopics(String[] topics){
        ITEMS.clear();
        for(String topic: topics){
            addItem(createHelpTopic(topic));
        }
    }

    private static void addItem(HelpTopic item) {
        ITEMS.add(item);
    }

    private static HelpTopic createHelpTopic(String topic) {
        return new HelpTopic(topic);
    }

    /**
     * A class representing a help topic
     * Right now only contains a String with the topic itself, but could easily be extended to
     * include more information. (E.g. if topics are made searchable)
     */
    public static class HelpTopic {
        public final String topic;

        public HelpTopic(String topic) {
            this.topic = topic;
        }

        @Override
        public String toString() {
            return topic;
        }
    }
}
