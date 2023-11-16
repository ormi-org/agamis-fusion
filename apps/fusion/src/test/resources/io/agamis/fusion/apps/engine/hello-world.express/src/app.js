const express = require('express');
const app = express();
const port = 3000;

app.get('/', (req, res) => {
  res.send('Hello World!');
});

app.listen(port, 'localhost', () => {
  slf4j.info(`Hello world app listening on port ${port}`);
});
