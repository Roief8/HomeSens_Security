// Importing all the models that need to 
const express = require("express");
const cors = require("cors");
const app = express();
app.use(express.json());
app.use(cors());

// Concting and Congigutrate the DataBase
const firebase = require("firebase");
const firebaseConfig = {
  apiKey: "AIzaSyBlmZOM63S0E_W_3e-6r4jGTZ2O6pIhBgs",
  authDomain: "esp8266-c9c43.firebaseapp.com",
  databaseURL: "https://esp8266-c9c43-default-rtdb.firebaseio.com",
  projectId: "esp8266-c9c43",
  storageBucket: "esp8266-c9c43.appspot.com",
  messagingSenderId: "538216873456",
  appId: "1:538216873456:web:f1c1e1383f1c2735e1f135",
  measurementId: "G-LTWSWSGHFV",
};

const firebaseApp = firebase.initializeApp(firebaseConfig);

const db = firebaseApp.firestore();
// Creating the Schema
const admin = require("firebase-admin");
const log = db.collection("log");

app.get("/", (req, res) => {
  res.send("Home Page for our project ");
});


app.post("/addLogs", async (req, res) => {
  const data = req.body;
  console.log("Data of Alarms", data);
  data.date = firebase.firestore.FieldValue.serverTimestamp();
  await log.add(data);
  res.json({ msg: "Alarm added" });
});



app.listen(3000, (req, res) => {
  console.log("Server is up");
});