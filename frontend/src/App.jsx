

import "./App.css";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Cadastro from "./Pages/Cadastro.jsx";
import Inicio from "./Pages/Inicio.jsx";


function App() {


 return(
     <BrowserRouter>
       <Routes>
           <Route path="/" element={<Inicio />} />
            <Route path="/cadastro" element={<Cadastro />} />
         {/* outras rotas */}
       </Routes>
     </BrowserRouter>
 );

}

export default App;