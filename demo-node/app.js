var express = require('express');
var request = require("request");
let ca = require('win-ca')

var app = express();
app.get('/', function (req, res) {
  
  request('https://consul.altostratus.me/demo/hello', (err, resp, body) => {
     if (err) { 
       res.send(err);
       return console.log(err); 
     }
     console.log(body.url);
     console.log(body.explanation);
     res.send('Hello invoked and received:' + body);
  });
});
var port = process.env.PORT || 8000;
app.listen(port, function () {
  console.log("Server running at http://localhost:%d", port);
});