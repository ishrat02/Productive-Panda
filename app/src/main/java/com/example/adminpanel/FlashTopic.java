package com.example.adminpanel;

public class FlashTopic {
    String topicId;
    String topicName;
    int noOfCards;
    public FlashTopic(){}
    public FlashTopic(String topicName, String topicId,int noOfCards) {
        this.topicName = topicName;
        this.topicId = topicId;
        this.noOfCards=noOfCards;
    }

    public int getNoOfCards() {
        return noOfCards;
    }

    public void setNoOfCards(int noOfCards) {
        this.noOfCards = noOfCards;
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }
}
