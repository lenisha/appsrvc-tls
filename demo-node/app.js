var express = require('express');
var app = express();
app.get('/', function (req, res) {
  res.send('Hello World!  Use Azure!');
});
var port = process.env.PORT || 8000;
app.listen(port, function () {
  console.log("Server running at http://localhost:%d", port);
});