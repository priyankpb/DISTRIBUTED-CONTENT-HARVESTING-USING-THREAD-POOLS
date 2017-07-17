package cs455.harvester;

import java.util.Objects;

/**
 *
 * @author YANK
 */
public class Task {

    private String url;
    private int recursionLevel;

    public String getUrl() {
        return url;
    }

    public Task() {
    }

    public Task(String url, int recursionLevel) {
        this.url = url;
        this.recursionLevel = recursionLevel;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getRecursionLevel() {
        return recursionLevel;
    }

    public void setRecursionLevel(int recursionLevel) {
        this.recursionLevel = recursionLevel;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.url);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Task other = (Task) obj;
        if (!Objects.equals(this.url, other.url)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Task{" + "url=" + url + '}';
    }

}
