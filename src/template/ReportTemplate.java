package template;

// Defines the skeleton for report generation using the Template Method pattern
public abstract class ReportTemplate<T> {

    // Orchestrates the report generation steps in a fixed order
    public final T generate() {
        gatherData();
        calculateTotals();
        return buildReport();
    }

    protected abstract void gatherData();

    protected abstract void calculateTotals();

    protected abstract T buildReport();
}
