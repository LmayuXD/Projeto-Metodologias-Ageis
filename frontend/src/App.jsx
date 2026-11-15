
import "./App.css";
import "./Pages/Cadastro.css"
import "./Pages/Editarperfil.css"
import "./Pages/Extrato.css"
import "./Pages/Inicio.css"
import "./Pages/Login.css"
import "./Pages/Poupanca.css"
import "./Pages/Transferencia.css"

import {BrowserRouter, Routes, Route} from "react-router-dom";
import Cadastro from "./Pages/Cadastro.jsx";
import Inicio from "./Pages/Inicio.jsx";
import Transferencia from "./Pages/Transferencia.jsx";
import Poupanca from "./Pages/Poupanca.jsx";
import Extrato from "./Pages/Extrato.jsx";
import EditarPerfil from "./Pages/EditarPerfil.jsx";
import Login from "./Pages/Login.jsx";

function App() {


 return(
     <BrowserRouter>
       <Routes>
            <Route path="/" element={<Inicio />} />

            <Route path="/cadastro" element={<Cadastro />} />

            <Route path="/login" element={< Login />} />

            <Route path="/editarperfil" element={<EditarPerfil />} />

            <Route path="/transferencia" element={<Transferencia />} />

            <Route path="/poupanca" element={<Poupanca />} />

            <Route path="/extrato" element={<Extrato />} />

       </Routes>
     </BrowserRouter>
 );

}

export default App;