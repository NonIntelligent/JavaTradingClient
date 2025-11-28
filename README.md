# The Project
![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/NonIntelligent/JavaTradingClient/autoTest.yml)
![Dynamic Regex Badge](https://img.shields.io/badge/dynamic/regex?url=https%3A%2F%2Fraw.githubusercontent.com%2FNonIntelligent%2FJavaTradingClient%2Frefs%2Fheads%2Fmain%2Fpom.xml&search=%3Cversion%3E(.*)%3C%2Fversion%3E&replace=%241&label=Version)
[![codecov](https://codecov.io/github/NonIntelligent/JavaTradingClient/graph/badge.svg?token=W3FYGEAXNO)](https://codecov.io/github/NonIntelligent/JavaTradingClient)
<p></p>
A market trading app to execute trades via market orders.
Connect to your trading API and perform market trades, view charts, order history, current positions, and current cash value.

# Technologies
Built using Maven as the build automation tool to streamline testing and manage dependencies.

- *JavaFX*: Easy to design and develop the UI
- *JUnit5*: Testing framework to reduce coding mistakes and minimise bugs on release.
- *JacksonDatabind*: Parse the JSON data from the APIs
- *SLF4J* (LOG4J2): Popular logging tool to gather runtime information behind an abstraction framework.
- *ChartFX*: Library extension for JavaFX charts that includes support for Financial charts.
