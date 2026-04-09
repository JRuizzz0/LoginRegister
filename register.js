const btnEnviarR = document.getElementById("enviarR");
const btnEnviarL = document.getElementById("enviarL");

btnEnviarR.addEventListener("click", (e) => { 
  e.preventDefault();
 
  const registro = {
    nombre: document.getElementById("nombreR").value.trim(),
    email: document.getElementById("emailR").value.trim(),
    contraseña: document.getElementById("contraseñaR").value.trim()
  };
 

  const datosR = { registro };
  const jsonEnvR = JSON.stringify(datosR);

  fetch("http://localhost:8080/dogs/register", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: jsonEnvR
  })
    .then(res => res.json())
    .then(data => console.log("Servidor dice:", data))
    .catch(err => console.error("Error en el envío:", err));
});   


btnEnviarL.addEventListener("click", (e) => {
  e.preventDefault();

  const login = {
    nombre: document.getElementById("nombreL").value.trim(),
    email: document.getElementById("emailL").value.trim(),
    contraseña: document.getElementById("contraseñaL").value.trim()
  };


  const datosL = { login };
  const jsonEnvL = JSON.stringify(datosL);

  fetch("http://localhost:8080/dogs/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: jsonEnvL
  })
    .then(res => res.json())
    .then(data => console.log("Servidor dice:", data))
    .catch(err => console.error("Error en el envío:", err));
});   

