## About the Project

This application is a specialized API designed to fetch, aggregate, and process statistical data from the Polish Central Statistical Office (**GUS BDL API**).

### Core Features & Goals
* **County Rankings:** The primary purpose of the service is to calculate and generate comprehensive rankings of Polish counties (*powiaty*) across various socio-economic dimensions (such as safety/crime rates, population dynamics, unemployment, and average salaries).
* **Predictive Analytics (Future Roadmap):** In the near future, the project will expand to include data forecasting. By utilizing the historical datasets retrieved from the BDL API, the system will implement predictive models to estimate future trends and statistics for individual regions.

---

## Data Continuity & Variable Mapping

To build accurate, long-term historical timelines, the application must adapt to how the external BDL API structures its data.

When the statistical office updates its research methodology or database schema over the years, they frequently issue **new variable IDs for the exact same metric**. To solve this and keep our rankings continuous, our internal `BdlVarId` configuration maps a single business indicator to a list of multiple external IDs.

### Example: The `CRIMES` Indicator
The crime rate metric requires two separate BDL identifiers to construct a seamless timeline:
* **`58559`** – Covers the historical data from **2013 to 2024**.
* **`1749155`** – Covers the recent and ongoing data from **2025 onwards**.

By tracking these as a collection, our import service automatically merges both data series, ensuring that county rankings remain accurate and unbroken across decade-long spans.