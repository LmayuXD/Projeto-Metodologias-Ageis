

import "./App.css";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Cadastro from "./Pages/Cadastro";
import Inicio from "./Pages/Inicio";


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