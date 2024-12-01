package tukano.impl.data;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Stats {

    @Id
    String shortId;
    int views;

    public Stats() {}

    public Stats(String shortId, int views) {
        this.shortId = shortId;
        this.views = views;
    }

    public String getShortId() {
        return shortId;
    }

    public void setShortId(String shortId) {
        this.shortId = shortId;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    @Override
    public String toString() {
        return "Stats [shortId=" + shortId + ", views=" + views + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(shortId, views);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Stats other = (Stats) obj;
        return Objects.equals(shortId, other.shortId) && views == other.views;
    }

}
