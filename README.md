# sleep-analytics-service
A Spring Boot backend that tracks user sleep data over time. It uses a custom midnight-offset algorithm to handle the "midnight wrap-around" problem, converting AM/PM bedtimes into raw minutes relative to midnight to ensure weekly and monthly averages are mathematically accurate.
