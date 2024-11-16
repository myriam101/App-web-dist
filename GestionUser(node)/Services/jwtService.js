const jwt = require('jsonwebtoken');
const crypto = require('crypto');

// Fonction pour générer une clé secrète aléatoire
const generateSecretKey = () => {
    return crypto.randomBytes(32).toString('hex'); // Générer une clé secrète de 32 octets
};

// Générer une clé secrète aléatoire à chaque démarrage de l'application
const secretKey = generateSecretKey();

// Fonction pour générer un token JWT
const generateToken = (userId) => {
    return jwt.sign({ id: userId }, secretKey, { expiresIn: '1h' });
};

// Fonction pour vérifier le token JWT
const verifyToken = (token) => {
    try {
        return jwt.verify(token, secretKey);
    } catch (error) {
        throw new Error('Token is not valid');
    }
};

// Exporter les fonctions
module.exports = {
    generateToken,
    verifyToken,
};
