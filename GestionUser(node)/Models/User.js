const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
    username: { type: String, required: true, unique: true, index: true },  // Index unique pour username
    firstName: { type: String, required: true, index: true },               // Index pour firstName
    lastName: { type: String, required: true, index: true },                // Index pour lastName
    email: { type: String, required: true, unique: true, index: true },     // Index unique pour email
    password: { type: String, required: true },
    image: { type: String, index: true },                                   // Index pour image
    sexe: { type: String, index: true },                                    // Index pour sexe
    dateOfBirth: { type: Date, index: true },                               // Index pour dateOfBirth
    dateOfCreation: { type: Date, default: Date.now, index: true },         // Index pour dateOfCreation
    nbHoursMaxPerWeek: { type: Number, default: 0, index: true },           // Index pour nbHoursMaxPerWeek
    nbHoursPerWeek: { type: Number, default: 0, index: true },              // Index pour nbHoursPerWeek
    role: { 
        type: String, 
        enum: ['Teacher', 'Student', 'Moderator', 'Admin', 'Recruiter'],
        index: true                                                         // Index pour role
    },
    companyName: { type: String, index: true },                             // Index pour companyName
    descriptionRecruiter: { type: String },
    scoreXp: { type: Number, default: 0, index: true },                     // Index pour scoreXp
    level: { type: Number, default: 0, index: true },                       // Index pour level
    overAllAverage: { type: Number, default: 0, index: true },              // Index pour overAllAverage
    speciality: { 
        type: String, 
        enum: ['BD', 'Angular', 'Spring', 'DotNet', 'Reseau', 'IA', 'Mobile', 'Web', 'Cloud', 'DevOps', 'Security', 'Design', 'Management', 'Marketing', 'Finance'],
        index: true                                                         // Index pour speciality
    },
    approved: { type: Boolean, default: false, index: true },               // Index pour approved
    validVoteCount: { type: Number, default: 0, index: true },              // Index pour validVoteCount
    canVote: { type: Boolean, default: true, index: true },                 // Index pour canVote
    nbVoteForIncentives: { type: Number, default: 0, index: true },         // Index pour nbVoteForIncentives
    nbPrimeVoteForBadge: { type: Number, default: 0, index: true },         // Index pour nbPrimeVoteForBadge
    PaymentDay: { type: Date, index: true },                                // Index pour PaymentDay
    Hobbies: { type: String, index: true },                                 // Index pour Hobbies
    badges: { type: [String], enum: ['GOLD', 'SILVER', 'BRONZE', 'DIMOND'], index: true }, // Index pour badges
    questionForums: [{ type: mongoose.Schema.Types.ObjectId, ref: 'QuestionForum', index: true }] // Index pour questionForums
});

// Créer les index automatiquement au démarrage
userSchema.set('autoIndex', true);

// Déclarer le modèle User
const User = mongoose.model('User', userSchema);

module.exports = User;
