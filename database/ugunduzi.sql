-- phpMyAdmin SQL Dump
-- version 4.2.6
-- http://www.phpmyadmin.net
--
-- Servidor: 192.168.86.55
-- Tiempo de generación: 06-03-2018 a las 13:07:10
-- Versión del servidor: 5.5.57-0+deb7u1-log
-- Versión de PHP: 5.3.29-1~dotdeb.0

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Base de datos: `ugunduzi`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `configuration`
--

CREATE TABLE IF NOT EXISTS `configuration` (
`configuration_id` int(10) unsigned NOT NULL,
  `farm_id` int(10) unsigned NOT NULL,
  `valid_from` date NOT NULL,
  `valid_until` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `crop`
--

CREATE TABLE IF NOT EXISTS `crop` (
`crop_id` int(10) unsigned NOT NULL,
  `crop_name` varchar(30) NOT NULL,
  `crop_variety` varchar(30) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `crop_x_plot`
--

CREATE TABLE IF NOT EXISTS `crop_x_plot` (
`crop_x_plot_id` int(10) unsigned NOT NULL,
  `crop_id` int(10) unsigned NOT NULL,
  `plot_id` int(10) unsigned NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `data_item`
--

CREATE TABLE IF NOT EXISTS `data_item` (
`data_item_id` int(10) unsigned NOT NULL,
  `data_item_name` varchar(100) NOT NULL,
  `data_item_default_units_id` int(10) unsigned NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `farm`
--

CREATE TABLE IF NOT EXISTS `farm` (
`farm_id` int(10) unsigned NOT NULL,
  `user_id` int(10) unsigned NOT NULL,
  `farm_name` varchar(30) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `log`
--

CREATE TABLE IF NOT EXISTS `log` (
`log_id` int(10) unsigned NOT NULL,
  `plot_id` int(10) unsigned NOT NULL,
  `log_date` date NOT NULL,
  `log_data_item_id` int(10) unsigned NOT NULL,
  `log_value` int(10) unsigned NOT NULL,
  `log_units_id` int(10) unsigned NOT NULL,
  `log_picture` varchar(100) NOT NULL,
  `log_sound` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `plot`
--

CREATE TABLE IF NOT EXISTS `plot` (
`plot_id` int(10) unsigned NOT NULL,
  `configuration_id` int(10) unsigned NOT NULL,
  `plot_x` int(10) unsigned NOT NULL,
  `plot_y` int(10) unsigned NOT NULL,
  `plot_w` int(10) unsigned NOT NULL,
  `plot_h` int(10) unsigned NOT NULL,
  `plot_size` int(10) unsigned NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `treatment`
--

CREATE TABLE IF NOT EXISTS `treatment` (
`treatment_id` int(10) unsigned NOT NULL,
  `treatment_name` varchar(30) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `treatment_x_plot`
--

CREATE TABLE IF NOT EXISTS `treatment_x_plot` (
`treatment_x_plot_id` int(10) unsigned NOT NULL,
  `treatment_id` int(10) unsigned NOT NULL,
  `plot_id` int(10) unsigned NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `units`
--

CREATE TABLE IF NOT EXISTS `units` (
`units_id` int(10) unsigned NOT NULL,
  `units_name` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `user`
--

CREATE TABLE IF NOT EXISTS `user` (
`user_id` int(10) unsigned NOT NULL,
  `user_name` varchar(100) NOT NULL,
  `user_alias` varchar(30) NOT NULL,
  `user_password` varchar(30) NOT NULL,
  `user_mobile` varchar(30) NOT NULL,
  `user_group` varchar(30) NOT NULL,
  `user_association` varchar(30) NOT NULL,
  `user_location` varchar(30) NOT NULL
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=2 ;

--
-- Volcado de datos para la tabla `user`
--

INSERT INTO `user` (`user_id`, `user_name`, `user_alias`, `user_password`, `user_mobile`, `user_group`, `user_association`, `user_location`) VALUES
(1, 'Test user', 'test', 'test', '', '', '', '');

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `configuration`
--
ALTER TABLE `configuration`
 ADD PRIMARY KEY (`configuration_id`);

--
-- Indices de la tabla `crop`
--
ALTER TABLE `crop`
 ADD PRIMARY KEY (`crop_id`);

--
-- Indices de la tabla `crop_x_plot`
--
ALTER TABLE `crop_x_plot`
 ADD PRIMARY KEY (`crop_x_plot_id`);

--
-- Indices de la tabla `data_item`
--
ALTER TABLE `data_item`
 ADD PRIMARY KEY (`data_item_id`);

--
-- Indices de la tabla `farm`
--
ALTER TABLE `farm`
 ADD PRIMARY KEY (`farm_id`);

--
-- Indices de la tabla `log`
--
ALTER TABLE `log`
 ADD PRIMARY KEY (`log_id`);

--
-- Indices de la tabla `plot`
--
ALTER TABLE `plot`
 ADD PRIMARY KEY (`plot_id`);

--
-- Indices de la tabla `treatment`
--
ALTER TABLE `treatment`
 ADD PRIMARY KEY (`treatment_id`);

--
-- Indices de la tabla `treatment_x_plot`
--
ALTER TABLE `treatment_x_plot`
 ADD PRIMARY KEY (`treatment_x_plot_id`);

--
-- Indices de la tabla `units`
--
ALTER TABLE `units`
 ADD PRIMARY KEY (`units_id`);

--
-- Indices de la tabla `user`
--
ALTER TABLE `user`
 ADD PRIMARY KEY (`user_id`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `configuration`
--
ALTER TABLE `configuration`
MODIFY `configuration_id` int(10) unsigned NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT de la tabla `crop`
--
ALTER TABLE `crop`
MODIFY `crop_id` int(10) unsigned NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT de la tabla `crop_x_plot`
--
ALTER TABLE `crop_x_plot`
MODIFY `crop_x_plot_id` int(10) unsigned NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT de la tabla `data_item`
--
ALTER TABLE `data_item`
MODIFY `data_item_id` int(10) unsigned NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT de la tabla `farm`
--
ALTER TABLE `farm`
MODIFY `farm_id` int(10) unsigned NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT de la tabla `log`
--
ALTER TABLE `log`
MODIFY `log_id` int(10) unsigned NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT de la tabla `plot`
--
ALTER TABLE `plot`
MODIFY `plot_id` int(10) unsigned NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT de la tabla `treatment`
--
ALTER TABLE `treatment`
MODIFY `treatment_id` int(10) unsigned NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT de la tabla `treatment_x_plot`
--
ALTER TABLE `treatment_x_plot`
MODIFY `treatment_x_plot_id` int(10) unsigned NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT de la tabla `units`
--
ALTER TABLE `units`
MODIFY `units_id` int(10) unsigned NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT de la tabla `user`
--
ALTER TABLE `user`
MODIFY `user_id` int(10) unsigned NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=2;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
