// server.js
const express = require('express');
const Eureka = require('eureka-js-client').Eureka;
const connectDB = require('./configurations/db');
const authRoutes = require('./Routes/AuthRoutes');

// Créer une instance Eureka client
const client = new Eureka({
    instance: {
        app: 'USER', // Nom de l'application
        instanceId: `USER:${Math.random().toString(36).substring(2, 15)}`, // ID unique
        hostName: 'localhost', // Adresse de l'hôte
        ipAddr: '127.0.0.1', // Ajout de l'adresse IP ici
        port: {
            '$': 4000, // Port de l'application
            '@enabled': true // Activer le port
        },
        vipAddress: 'USER', // Adresse VIP pour l'application
        secure: false,
        statusPageUrl: 'http://localhost:4000/health', // URL pour vérifier la santé
        healthCheckUrl: 'http://localhost:4000/health', // URL de vérification de santé
        homePageUrl: 'http://localhost:4000', // URL de la page d'accueil
        dataCenterInfo: {
            name: 'MyOwn', // Nom du data center
            '@class': 'com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo',
        }
    },
    eureka: {
        host: 'localhost',
        port: 8761, // Port du serveur Eureka
        servicePath: '/eureka/apps/'
    }
});

// Démarrer le client Eureka
client.start((error) => {
    if (error) {
        console.error('Error starting the Eureka Client', error);
    } else {
        console.log('Eureka client started');
    }
});

// Ajouter un endpoint de santé
const app = express();
const PORT = 4000; // Port pour le serveur Express

app.get('/health', (req, res) => {
    res.status(200).send('OK'); // Réponse de vérification de santé
});

// Middleware pour parser le JSON
app.use(express.json());

// Connecter à MongoDB
connectDB();

// Routes
app.use('/api/auth', authRoutes);


// Démarrer le serveur Express
app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});

// Gestion de l'arrêt propre
process.on('SIGINT', () => {
    console.log('Gracefully shutting down');
    
    // Dé-enregistrer le service de Eureka
    client.stop(() => {
        console.log('Eureka client stopped');
        process.exit(0);
    });
});
