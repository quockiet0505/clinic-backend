const mysql = require('mysql2');

const connection = mysql.createConnection({
  host: 'localhost',
  user: 'root',
  password: '12345678',
  database: 'clinic_system'
});

connection.execute(
  'UPDATE service_order so JOIN service s ON so.service_id = s.service_id SET so.price_at_time = COALESCE(s.discount_price, s.original_price) WHERE so.price_at_time IS NULL OR so.price_at_time = 0',
  (err, results) => {
    if (err) {
      console.error('Error executing update:', err);
    } else {
      console.log('Update success! Rows affected:', results.affectedRows);
    }
    connection.end();
  }
);
