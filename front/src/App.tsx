import React, {UIEventHandler, useCallback, useMemo, useState} from 'react';
import DiscordAuth from "./DiscordAuth.tsx";
import {createBrowserRouter, Link, Outlet, RouterProvider} from "react-router-dom";
import Gif from "./Pages/Gif.tsx";
import Home from "./Pages/Home.tsx";
import "./App.css"
import API from "./api/api.ts";
import UserGifs from "./Pages/UserGifs.tsx";

function App() {
    const [bottom, setBottom] = useState(false);
    const api = useMemo(() => new API(import.meta.env.VITE_API), []);
    const handleScroll = useCallback((e: React.UIEvent<HTMLDivElement>) => {
        const target = e.currentTarget;
        if(target.scrollHeight - target.scrollTop === target.clientHeight) {
            setBottom(true)
        } else {
            setBottom(false);
        }
    }, []);

    const router = createBrowserRouter([
        {
            path: "/",
            element: <Root handleScroll={handleScroll} setBottom={setBottom} api={api}/>,
            children: [
                {
                    path: "/",
                    element: <Home api={api} bottom={bottom}/>,
                },
                {
                    path: "gif/:id",
                    element: <Gif api={api}/>
                },
                {
                    path: "gif/me",
                    element: <UserGifs api={api} bottom={bottom}/>
                }
            ]
        }
    ]);

    return (
        <RouterProvider router={router}/>
    )
}

interface RootProps {
    handleScroll: UIEventHandler<HTMLDivElement>,
    setBottom: React.Dispatch<React.SetStateAction<boolean>>,
	api: API
}

function Root(props: RootProps) {
    const [user, setUser] = useState<User|null>(null);
    const [userToken, setUserToken] = useState<string|null>(null);

    return (
        <>
            <div className={"w-screen h-screen max-h-screen flex flex-col"}>
                <div className={"w-full grid grid-cols-3 justify-between bg-black items-center px-8 py-2 drop-shadow-header"}>
                    <Link className={"text-neutral-100 text-3xl grow"} to={"/"}>
                        Kaamelott - gif
                    </Link>
                    <div className={"flex justify-center"}>
                        {user !== null &&
                            <Link to={"/gif/me"} className={"text-neutral-100"}>Mes gifs</Link>
                        }
                    </div>
                    <div className={"grow flex justify-end"}>
                        <DiscordAuth
                            user={user}
                            setUser={setUser}
                            token={userToken}
                            setToken={setUserToken}
                            redirectUri={import.meta.env.VITE_REDIRECT}
                            clientId={import.meta.env.VITE_CLIENT_ID}
                            scope={"identify"}
							api={props.api}
                        />
                    </div>
                </div>
                <div className={"grow bg-neutral-400 py-4 relative overflow-auto"} onScroll={props.handleScroll} onLoad={e => {
                    if(e.currentTarget.scrollHeight === e.currentTarget.clientHeight) {
                        props.setBottom(true);
                    }
                }}>
                    <div className={"absolute top-0 left-0 w-full h-full bg-logo z-0"}/>
                    <div className={"relative z-10 w-full flex flex-col items-center"}>
                        <Outlet/>
                    </div>
                </div>
                <div className={"bg-black text-neutral-100 flex justify-center px-8 py-2 drop-shadow-footer"}>
                    Fait avec <span className={"text-red-600 px-1"}>♥</span> par imacaron
                </div>
            </div>
        </>
    )
}

export default App
