// services/UserService.js
const jwtService = require('./jwtService'); // Import du service JWT
const User = require('../Models/User'); // Assurez-vous que le chemin d'accès est correct
const bcrypt = require('bcryptjs'); // Importer bcryptjs

class UserService {

    // register 
    async registerUser(userData) {
        // Hacher le mot de passe
        const hashedPassword = await bcrypt.hash(userData.password, 10);
        userData.password = hashedPassword;

        // Créer l'utilisateur
        const newUser = new User(userData);
        await newUser.save(); // Enregistrer l'utilisateur dans la base de données

        return newUser;
    }

   

 // Méthode pour se connecter
 async loginUser(email, password) {
    console.log(`Trying to login with email: ${email}`); // Debugging
    const user = await User.findOne({ email });
    if (!user) {
        console.error('User not found'); // Debugging
        throw new Error('Invalid email');
    }

    const isMatch = await bcrypt.compare(password, user.password);
    console.log(`Password match: ${isMatch}`); // Debugging
    if (!isMatch) {
        console.error('Password does not match'); // Debugging
        throw new Error('Invalid password');
    }

    // Générer le token JWT
    const token = jwtService.generateToken(user._id);
    return { user, token }; // Retourne l'utilisateur et le token
}












    // Méthode pour obtenir tous les utilisateurs
    async getAllUsers() {
        return await User.find();
    }

    // Méthode pour obtenir un utilisateur par ID
    async getUserById(userId) {
        return await User.findById(userId);
    }

    // Méthode pour mettre à jour un utilisateur
    async updateUser(userId, userData) {
        if (userData.password) {
            const hashedPassword = await bcrypt.hash(userData.password, 10);
            userData.password = hashedPassword;
        }
        return await User.findByIdAndUpdate(userId, userData, { new: true });
    }

    // Méthode pour supprimer un utilisateur
    async deleteUser(userId) {
        return await User.findByIdAndDelete(userId);
    }

}

module.exports = new UserService();
