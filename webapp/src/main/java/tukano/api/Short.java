package tukano.api;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import tukano.impl.Token;

/**
 * Represents a Short video uploaded by an user.
 *
 * A short has an unique shortId and is owned by a given user;
 * Comprises of a short video, stored as a binary blob at some bloburl;.
 * A post also has a number of likes, which can increase or decrease over time.
 * It is the only piece of information that is mutable.
 * A short is timestamped when it is created.
 *
 */
@Entity
public class Short {

    @Id String shortId;
    String ownerId;
    String blobUrl;
    long timestamp;
    int totalLikes;
    int totalViews;

    public Short() {}

    public Short(String shortId, String ownerId, String blobUrl, long timestamp,
                 int totalLikes, int totalViews) {
        super();
        this.shortId = shortId;
        this.ownerId = ownerId;
        this.blobUrl = blobUrl;
        this.timestamp = timestamp;
        this.totalLikes = totalLikes;
        this.totalViews = totalViews;
    }

    public Short(String shortId, String ownerId, String blobUrl) {
        this(shortId, ownerId, blobUrl, System.currentTimeMillis(), 0, 0);
    }

    public String getShortId() { return shortId; }

    public void setShortId(String shortId) { this.shortId = shortId; }

    public String getOwnerId() { return ownerId; }

    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getBlobUrl() { return blobUrl; }

    public void setBlobUrl(String blobUrl) { this.blobUrl = blobUrl; }

    public long getTimestamp() { return timestamp; }

    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getTotalLikes() { return totalLikes; }

    public void setTotalLikes(int totalLikes) { this.totalLikes = totalLikes; }

    public int getTotalViews() { return totalViews; }

    public void setTotalViews(int totalViews) { this.totalViews = totalViews; }

    @Override
    public String toString() {
        return "Short [shortId=" + shortId + ", ownerId=" + ownerId +
            ", blobUrl=" + blobUrl + ", timestamp=" + timestamp +
            ", totalLikes=" + totalLikes + ", totalViews=" + totalViews +"]";
    }

    public Short copyWithLikes_Views_And_Token(long totLikes, long totViews) {
        var urlWithToken =
            String.format("%s?token=%s", blobUrl, Token.get(shortId));
        return new Short(shortId, ownerId, urlWithToken, timestamp,
                         (int)totLikes, (int)totViews);
    }
}
