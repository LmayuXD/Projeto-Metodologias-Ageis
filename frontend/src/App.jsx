

import "./App.css";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Cadastro from "./Pages/Cadastro.jsx";
import Inicio from "./Pages/Inicio.jsx";
import Perfil from "./Pages/Perfil.jsx";
import Transferencia from "./Pages/Transferencia.jsx";
import Poupanca from "./Pages/Poupanca.jsx";
import Extrato from "./Pages/Extrato.jsx";


function App() {


 return(
     <BrowserRouter>
       <Routes>
            <Route path="/" element={<Inicio />} />

            <Route path="/cadastro" element={<Cadastro />} />

            <Route path="/perfil" element={<Perfil />} />

            <Route path="/transferencia" element={<Transferencia />} />

            <Route path="/poupanca" element={<Poupanca />} />

            <Route path="/extrato" element={<Extrato />} />


       </Routes>
     </BrowserRouter>
 );

}

export default App;