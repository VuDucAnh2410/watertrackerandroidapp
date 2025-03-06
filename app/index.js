const express = require('express');
const bodyParser = require('body-parser');
const cors = require('cors');
const sql = require('mssql');

const app = express();
app.use(cors());
app.use(bodyParser.json());

const config = {
    user: 'your_db_username',
    password: 'your_db_password',
    server: 'your_db_server', // Ví dụ: localhost hoặc IP
    database: 'WaterTracker',
    options: {
        encrypt: true, // Sử dụng cho Azure
        trustServerCertificate: true // Chỉ nếu không sử dụng SSL
    }
};

sql.connect(config).then(pool => {
    if (pool.connected) {
        console.log('Connected to SQL Server');

        app.post('/register', async (req, res) => {
            const { user_id, phone_number, email, password } = req.body;
            try {
                const result = await pool.request()
                    .input('user_id', sql.VarChar(15), user_id)
                    .input('phone_number', sql.VarChar(11), phone_number)
                    .input('email', sql.VarChar(100), email)
                    .input('password', sql.VarChar(100), password)
                    .query('INSERT INTO ACCOUNT (user_id, phone_number, email, password) VALUES (@user_id, @phone_number, @email, @password)');
                res.status(201).send('User registered successfully');
            } catch (error) {
                res.status(400).send(error.message);
            }
        });

        // Thêm các route khác cho USERS, WATER_INTAKE và Reminders ở đây

        app.listen(3000, () => {
            console.log('Server running on port 3000');
        });
    }
}).catch(err => {
    console.error('SQL Server connection error', err);
});