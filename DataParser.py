import pandas as pd

# # Remove any trains after March 1st:
# data = pd.read_csv("services-2025-03.csv")
#
# firstDay = data[data["Service:Date"] == "3/1/2025"]
# firstDay.to_csv("day.csv", index=False)

# # Remove the service date column
# day = pd.read_csv("day.csv")
# day = day.drop(columns=["Service:Date"])
# day.to_csv("day2.csv", index=False)

# # Remove delay information and real platform information to clean up csv
# day = pd.read_csv("day2.csv")
# day = day.drop(columns=["Stop:Arrival delay", "Stop:Actual platform"])
# day.to_csv("dayFinal.csv", index=False)

# We can actually also remove the first column
day = pd.read_csv("dayFinal.csv")
day = day.drop(columns=["Service:RDT-ID"])
day.to_csv("dayFinalForRealThisTime.csv", index=False)
