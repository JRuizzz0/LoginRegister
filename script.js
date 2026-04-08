const cont = document.getElementById("imgContainer");
const btn = document.getElementById("cargar");


let q = []; 

btn.addEventListener("click", (e) => {  
  e.preventDefault();
  cont.innerHTML = "";
  respuestasEnv = []; 
  respuestas_correctas = [];

  fetch("http://localhost:8080/dogs/list/razas") 
    .then(result => result.json())
    .then(data => {
      q = data; 
      q.forEach((i, index) => {
        const pregunta = document.createElement("h3");
        const subraza = document.createElement("p")
        pregunta.textContent = `${index + 1}. ${i.nombre || i.pregunta}`;
        subraza.textContent = `${index + 1}. ${i.subrazas || i.subraza}`;

        cont.appendChild(pregunta);
        cont.appendChild(subraza);
        });
    })
    .catch(error => {
      console.error("Error al obtener los datos:", error);
    });
});
