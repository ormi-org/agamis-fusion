const { randomUUID } = require('crypto');
const express = require('express');
const http = require('http');
const { WebSocketServer } = require('ws');

const app = express();
slf4j.debug('SOCKET_FILES_ROOT: ' + SOCKET_FILES_ROOT);
const httpSocketPath = SOCKET_FILES_ROOT + "/http-25171ea0_5c46bc8e.sock";
const wsSocketPath = SOCKET_FILES_ROOT + "/ws-25171ea0_b7728efc.sock";
const wsHttpServer = http.createServer();
const ws = new WebSocketServer({ server: wsHttpServer });

// Define routes
app.get('/', (req, res) => {
  res.send('Hello World!');
});

// Define messages
ws.on('connection', client => {
  client.id = randomUUID();
  // Log connection open
  slf4j.info(`New connection with client-${client.id}`);
  // ACK connection open
  client.send('ACCEPT');
  // Handle connection closed log
  client.on('close', () => {
    slf4j.info(`client-${client.id} closed connection`);
  });
  // Handle msg
  client.on('message', msg => {
    if (msg.toString() === "PING") {
      client.send("PONG");
      return;
    }
    slf4j.info(`received unhandled message from client-${client.id}: ${msg.toString()}`);
  });
});

// Start servers
app.listen(httpSocketPath, () => {
  slf4j.info(`Hello world app listening on socket ${httpSocketPath}`);
});

wsHttpServer.listen(wsSocketPath, () => {
  slf4j.info(`Hello world app WebSocket listening on socket ${wsSocketPath}`);
});
