import * as admin from "firebase-admin";
import {logger} from "firebase-functions/v2";
import {onSchedule} from "firebase-functions/v2/scheduler";

const app = admin.initializeApp();

const exchangeRatesApiKey = process.env["EXCHANGE_RATE_API"];

export const fetchConversionRates = onSchedule("0 0 * * *", async () => {
  try {
    const uri = `https://v6.exchangerate-api.com/v6/${exchangeRatesApiKey}/latest/USD`;
    const response = await fetch(uri);

    if (!response.ok) {
      logger.error(response.status);
      return;
    }

    const data = await response.json();

    await Promise.all([
      app.database()
        .ref("last_updated")
        .set(data["time_last_update_unix"]),
      app.database()
        .ref("conversion_rates")
        .update(data["conversion_rates"]),
    ]);
  } catch (e) {
    logger.error(e);
  }
});
