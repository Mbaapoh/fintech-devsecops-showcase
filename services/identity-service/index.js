const http = require('http');

console.log("Identity Service Starting...");

const server = http.createServer((req, res) => {
  res.writeHead(200, { 'Content-Type': 'application/json' });
  res.end(JSON.stringify({ status: "Authenticated" }));
});

server.listen(3000, () => {
    console.log('Server running on port 3000');
});
