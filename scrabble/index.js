const prevBoard = [
    [".", "D", "."],
    [".", "I", "."],
    [".", "G", "."],
    [".", ".", "."],
    [".", ".", "."],
    [".", ".", "."],
    [".", ".", "."],
];
// [DIG]

const arr = [
    [".", "D", "."],
    [".", "I", "."],
    [".", "G", "."],
    [".", ".", "."],
    [".", "B", "."],
    [".", "I", "."],
    [".", "G", "."],
];
// [BIG, DIG]

const firstWord = { x: 0, y: 1 }; // baris pertama
const secondWord = { x: 2, y: 1 }; // baris kedua
let str = "";

if (firstWord.x === secondWord.x) {
    // Jika x sama, berarti kata berada di baris yang sama
    for (let i = firstWord.y; i <= secondWord.y; i++) {
        str += arr[firstWord.x][i];
    }
} else {
    for (let i = firstWord.x; i <= secondWord.x - firstWord.x; i++) {
        str += arr[i][firstWord.y];
    }
}

console.log(str);
