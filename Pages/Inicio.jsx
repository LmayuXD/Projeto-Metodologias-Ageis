import "../App.css";
import { Link } from "react-router-dom";
import { useState } from "react";

export default function Inicio(){

return (
   <div>
       <h2> SEJA BEM-VINDO(A) </h2>

       <Link class="botao-inicio" to={"/cadastro"}>
           <button>INICIO</button>
       </Link>

       <footer class="cadastro-footer">
           <p> &copy; Todos os direitos reservados por Tech Girls. </p>
       </footer>

   </div>

);
}