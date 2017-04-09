package jason.infra;

/**
 * This interface is implemented by objects that wants to be notified
 * about changes in the MAS execution state (like JasonIDE).
 */
public interface RunProjectListener {
    public void masFinished();
}
