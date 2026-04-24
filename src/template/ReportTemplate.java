package template;

public abstract class ReportTemplate<T> {

    public final T generate() {
        gatherData();
        calculateTotals();
        return buildReport();
    }

    protected abstract void gatherData();

    protected abstract void calculateTotals();

    protected abstract T buildReport();
}
