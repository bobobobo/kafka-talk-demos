import React, { useState, useEffect } from "react";
import logo from "./logo.svg";
import Card from "./Card";
import { v4 as uuidv4 } from "uuid";

const pollFetch = (url, resultCallback) => {
  fetch(url)
    .then((response) => response.json())
    .then(resultCallback)
    .then(() => setTimeout(() => pollFetch(url, resultCallback), 1000));
};

const createOrderStub = () => ({
  id: uuidv4(),
  customerId: 12,
  productIds: [],
});
const initialProduct = "PRODUCT-1";

function App() {
  const [customers, setCustomers] = useState([]);
  useEffect(() => {
    pollFetch("http://localhost:8082/customer", setCustomers);
  }, []);

  const [products, setProducts] = useState([]);
  useEffect(() => {
    pollFetch("http://localhost:8082/product", setProducts);
  }, []);
  const [customerOrders, setCustomerOrders] = useState([]);
  useEffect(() => {
    pollFetch("http://localhost:8082/customerOrderCount", setCustomerOrders);
  }, []);
  const [orders, setOrders] = useState([]);
  useEffect(() => {
    pollFetch("http://localhost:8082/order", setOrders);
  }, []);

  const [selectedProduct, setSelectedProduct] = useState(initialProduct);
  const [newOrder, setNewOrder] = useState(createOrderStub());

  const setStatus = (order, status) => {
    fetch(`http://localhost:8081/order/${order}/${status}`, {
      method: "PUT",
    });
  };

  return (
    <div className="max-w-7xl mx-auto sm:px-6 lg:px-8">
      <div className="space-y-6">
        <Card title="Add order">
          <div className="px-4 py-5 sm:p-6">
            <div className="grid grid-cols-2 gap-4">
              <form>
                <div className="space-y-6">
                  <div>
                    <label
                      htmlFor="customer"
                      className="block text-sm font-medium text-gray-700"
                    >
                      Customer
                    </label>
                    <select
                      id="customer"
                      className="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm rounded-md"
                      value={newOrder.customerId}
                      onChange={(e) => {
                        setNewOrder({
                          ...newOrder,
                          customerId: e.target.value,
                        });
                      }}
                    >
                      {customers.map((customer) => (
                        <option key={customer.id} value={customer.id}>
                          {customer.name}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="space-y-2">
                    <div className="space-y-1">
                      <label
                        htmlFor="add_product"
                        className="block text-sm font-medium text-gray-700"
                      >
                        Products
                      </label>

                      <div className="flex">
                        <div className="flex-grow">
                          <select
                            id="add_product"
                            className="block w-full shadow-sm focus:ring-light-blue-500 focus:border-light-blue-500 sm:text-sm border-gray-300 rounded-md"
                            value={selectedProduct}
                            onChange={(e) => {
                              setSelectedProduct(e.target.value);
                            }}
                          >
                            {products.map((product) => (
                              <option key={product.id} value={product.id}>
                                {product.name}
                              </option>
                            ))}
                          </select>
                        </div>
                        <span className="ml-3">
                          <button
                            type="button"
                            className="bg-white inline-flex items-center px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-light-blue-500"
                            onClick={() => {
                              setNewOrder({
                                ...newOrder,
                                productIds: [
                                  ...newOrder.productIds,
                                  selectedProduct,
                                ],
                              });
                              setSelectedProduct(initialProduct);
                            }}
                          >
                            {/* Heroicon name: plus */}
                            <svg
                              className="-ml-2 mr-1 h-5 w-5 text-gray-400"
                              xmlns="http://www.w3.org/2000/svg"
                              viewBox="0 0 20 20"
                              fill="currentColor"
                              aria-hidden="true"
                            >
                              <path
                                fillRule="evenodd"
                                d="M10 5a1 1 0 011 1v3h3a1 1 0 110 2h-3v3a1 1 0 11-2 0v-3H6a1 1 0 110-2h3V6a1 1 0 011-1z"
                                clipRule="evenodd"
                              />
                            </svg>
                            <span>Add</span>
                          </button>
                        </span>
                      </div>
                    </div>
                  </div>

                  <div className="flex justify-end">
                    <button
                      type="submit"
                      className="ml-3 inline-flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-light-blue-500 hover:bg-light-blue-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-light-blue-500"
                      onClick={() => {
                        fetch("http://localhost:8080/order", {
                          method: "POST",
                          headers: {
                            "Content-Type": "application/json",
                          },
                          body: JSON.stringify(newOrder), // body data type must match "Content-Type" header
                        });
                        setNewOrder(createOrderStub());
                      }}
                    >
                      Create this order
                    </button>
                  </div>
                </div>
              </form>
              <div className="bg-gray-200">
                <span className="font-mono whitespace-pre">
                  {JSON.stringify(newOrder, null, 4)}
                </span>
              </div>
            </div>
          </div>
        </Card>
        <Card title="Orders">
          <div className="flex flex-col">
            <div className="-my-2 overflow-x-auto sm:-mx-6 lg:-mx-8">
              <div className="py-2 align-middle inline-block min-w-full sm:px-6 lg:px-8">
                <div className="shadow overflow-hidden border-b border-gray-200 sm:rounded-lg">
                  <table className="min-w-full divide-y divide-gray-200">
                    <thead>
                      <tr>
                        <th
                          scope="col"
                          className="px-6 py-3 bg-gray-50 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                        >
                          Id
                        </th>
                        <th
                          scope="col"
                          className="px-6 py-3 bg-gray-50 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                        >
                          Customer
                        </th>
                        <th
                          scope="col"
                          className="px-6 py-3 bg-gray-50 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                        >
                          Products
                        </th>
                        <th
                          scope="col"
                          className="px-6 py-3 bg-gray-50 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                        >
                          Status
                        </th>
                        <th
                          scope="col"
                          className="px-6 py-3 bg-gray-50 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                        ></th>
                      </tr>
                    </thead>
                    <tbody>
                      {orders.map((order) => (
                        <tr className="bg-white" key={order.id}>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                            {order.id}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            {order.customer.name}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            {order.products
                              .map((product) => product.name)
                              .join(", ")}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            {order.status}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                            {order.status === "CREATED" && (
                              <div className="flex flex-row space-x-4">
                                <a
                                  
                                  className="text-green-600 hover:text-green-900 cursor-pointer"
                                  onClick={() => {
                                    setStatus(order.id, "PAID");
                                    return false;
                                  }}
                                >
                                  Pay
                                </a>
                                <a
                                  
                                  className="text-red-600 hover:text-red-900 cursor-pointer"
                                  onClick={() => {
                                    setStatus(order.id, "CANCELED");
                                    return false;
                                  }}
                                >
                                  Cancel
                                </a>
                              </div>
                            )}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </Card>
        <Card title="Orders per customer">
          <div className="flex flex-col">
            <div className="-my-2 overflow-x-auto sm:-mx-6 lg:-mx-8">
              <div className="py-2 align-middle inline-block min-w-full sm:px-6 lg:px-8">
                <div className="shadow overflow-hidden border-b border-gray-200 sm:rounded-lg">
                  <table className="min-w-full divide-y divide-gray-200">
                    <thead>
                      <tr>
                        <th
                          scope="col"
                          className="px-6 py-3 bg-gray-50 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                        >
                          Customer
                        </th>
                        <th
                          scope="col"
                          className="px-6 py-3 bg-gray-50 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                        >
                          Order count
                        </th>
                      </tr>
                    </thead>
                    <tbody>
                      {customerOrders.map((customerOrderCount) => (
                        <tr
                          className="bg-white"
                          key={customerOrderCount.customerId}
                        >
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                            {customerOrderCount.customerId}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            {customerOrderCount.count}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </Card>
        <Card title="Customers">
          <div className="flex flex-col">
            <div className="-my-2 overflow-x-auto sm:-mx-6 lg:-mx-8">
              <div className="py-2 align-middle inline-block min-w-full sm:px-6 lg:px-8">
                <div className="shadow overflow-hidden border-b border-gray-200 sm:rounded-lg">
                  <table className="min-w-full divide-y divide-gray-200">
                    <thead>
                      <tr>
                        <th
                          scope="col"
                          className="px-6 py-3 bg-gray-50 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                        >
                          Id
                        </th>
                        <th
                          scope="col"
                          className="px-6 py-3 bg-gray-50 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                        >
                          Name
                        </th>
                        <th
                          scope="col"
                          className="px-6 py-3 bg-gray-50 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                        >
                          Customer number
                        </th>
                      </tr>
                    </thead>
                    <tbody>
                      {customers.map((customer) => (
                        <tr className="bg-white" key={customer.id}>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                            {customer.id}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            {customer.name}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            {customer.customerNumber}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </Card>
        <Card title="Products">
          <div className="flex flex-col">
            <div className="-my-2 overflow-x-auto sm:-mx-6 lg:-mx-8">
              <div className="py-2 align-middle inline-block min-w-full sm:px-6 lg:px-8">
                <div className="shadow overflow-hidden border-b border-gray-200 sm:rounded-lg">
                  <table className="min-w-full divide-y divide-gray-200">
                    <thead>
                      <tr>
                        <th
                          scope="col"
                          className="px-6 py-3 bg-gray-50 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                        >
                          Id
                        </th>
                        <th
                          scope="col"
                          className="px-6 py-3 bg-gray-50 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                        >
                          Name
                        </th>
                      </tr>
                    </thead>
                    <tbody>
                      {products.map((product) => (
                        <tr className="bg-white" key={product.id}>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                            {product.id}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            {product.name}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
}

export default App;
