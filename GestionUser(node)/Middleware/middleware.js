// Middleware pour vérifier le token JWT
const jwtService = require('../Services/jwtService');

const authMiddleware = (req, res, next) => {
    const token = req.headers['authorization']?.split(' ')[1]; // Récupérer le token depuis l'en-tête Authorization

    if (!token) {
        return res.status(403).send({ message: 'No token provided!' }); // Pas de token fourni
    }

    try {
        const decoded = jwtService.verifyToken(token); // Vérifier le token
        req.userId = decoded.id; // Stocker l'ID de l'utilisateur dans la requête
        next(); // Passer à la prochaine fonction middleware ou à la route
    } catch (error) {
        return res.status(401).send({ message: 'Unauthorized!' }); // Token invalide
    }
};

module.exports = authMiddleware;
