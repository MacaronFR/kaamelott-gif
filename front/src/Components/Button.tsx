import React, {ReactNode} from "react";
import {clsx} from "clsx";

interface ButtonProps {
	onClick?: React.MouseEventHandler<HTMLButtonElement>
	children: ReactNode,
	className?: string
}

export default function Button(props: ButtonProps) {
	return (
		<button
			onClick={props.onClick}
			className={clsx(props.className, "border-2 border-neutral-400 rounded text-black px-4 py-2 hover:bg-neutral-400 transition-all hover:scale-110 hover:text-neutral-100")}
		>
			{props.children}
		</button>
	);
}