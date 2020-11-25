export default (props) => (
  <div className="bg-white overflow-hidden shadow rounded-lg divide-y divide-gray-200">
    <div className="bg-white px-4 py-5 border-b border-gray-200 sm:px-6">
      <h3 className="text-lg leading-6 font-medium text-gray-900">
        {props.title}
      </h3>
    </div>
    {props.children}
  </div>
)